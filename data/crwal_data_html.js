const axios = require('axios');
const cheerio = require('cheerio');
const fs = require('fs');

const PRODUCTS_FILE = 'products.json';
const OUTPUT_FILE = 'product_detail.json';

if (!fs.existsSync(PRODUCTS_FILE)) {
	console.error('products.json not found. Run crawl-sitemap first.');
	process.exit(1);
}

const products = JSON.parse(fs.readFileSync(PRODUCTS_FILE, 'utf8'));
// LIMIT: if not provided, process all products; accepts a number or 'all'
let rawLimit = process.env.LIMIT || process.argv[2];
let LIMIT;
if (!rawLimit || String(rawLimit).trim() === '') {
	LIMIT = products.length;
} else if (String(rawLimit).toLowerCase() === 'all') {
	LIMIT = products.length;
} else {
	LIMIT = parseInt(rawLimit, 10) || products.length;
}
const CONCURRENCY = parseInt(process.env.CONCURRENCY || '5', 10);

function sleep(ms) { return new Promise(r => setTimeout(r, ms)); }

async function fetchHtml(url) {
	try {
		const res = await axios.get(url, {
			headers: { 'User-Agent': 'Mozilla/5.0' },
			timeout: 20000
		});
		return res.data;
	} catch (err) {
		console.error('Fetch error', url, err.message);
		return null;
	}
}

function extractLocFromJsonLd($) {
	const scripts = $('script[type="application/ld+json"]');
	for (let i = 0; i < scripts.length; i++) {
		const txt = $(scripts[i]).contents().text();
		try {
			const data = JSON.parse(txt);
			const items = Array.isArray(data) ? data : [data];
			for (const it of items) {
				if (it && (it['@type'] === 'Product' || (it.name && it.offers))) {
					return it;
				}
			}
		} catch (e) {
			// ignore parse errors
		}
	}
	return null;
}

function findLabelValue($, labelKeywords) {
	const regex = new RegExp(labelKeywords.join('|'), 'i');
	let found = null;
	$('*').each((i, el) => {
		const text = $(el).text().trim();
		if (!text) return;
		if (regex.test(text)) {
			// try inline "Label: value"
			const m = text.match(new RegExp('(?:' + labelKeywords.join('|') + ')\\s*[:\\-]\\s*(.+)$', 'i'));
			if (m && m[1]) { found = m[1].trim(); return false; }
			// try next sibling
			const next = $(el).next();
			if (next && next.text && next.text().trim()) { found = next.text().trim(); return false; }
			// try parent row
			const parent = $(el).parent();
			const t = parent.find('td,span,div').last().text().trim();
			if (t) { found = t; return false; }
		}
	});
	return found;
}

function extractImages($, jsonLd) {
	const imgs = new Set();
	if (jsonLd && jsonLd.image) {
		if (Array.isArray(jsonLd.image)) jsonLd.image.forEach(i => imgs.add(i));
		else imgs.add(jsonLd.image);
	}
	const og = $('meta[property="og:image"]').attr('content') || $('meta[name="og:image"]').attr('content');
	if (og) imgs.add(og);
	const tw = $('meta[property="twitter:image"]').attr('content') || $('meta[name="twitter:image"]').attr('content');
	if (tw) imgs.add(tw);
	const mp = $('meta[itemprop="image"]').attr('content');
	if (mp) imgs.add(mp);
	// common gallery selectors
	['.woocommerce-product-gallery img', '.product-gallery img', '.product-images img', '.gallery img', 'img'].forEach(sel => {
		$(sel).each((i, el) => {
			const s = $(el).attr('src') || $(el).attr('data-src') || $(el).attr('data-lazy-src');
			if (s && s.length > 10) imgs.add(s.split('?')[0]);
		});
	});
	return Array.from(imgs);
}

function extractCategories($, jsonLd) {
	// Try JSON-LD BreadcrumbList first
	try {
		const scripts = $('script[type="application/ld+json"]');
		for (let i = 0; i < scripts.length; i++) {
			const txt = $(scripts[i]).contents().text();
			if (!txt) continue;
			try {
				const data = JSON.parse(txt);
				const items = Array.isArray(data) ? data : [data];
				for (const it of items) {
					if (!it) continue;
					const type = it['@type'] || it['type'];
					if (type && type.toLowerCase && type.toLowerCase().includes('breadcrumb')) {
						const list = it.itemListElement || it.itemList || [];
						const names = list.map(el => {
							if (!el) return '';
							if (el.name) return el.name;
							if (el.item && el.item.name) return el.item.name;
							if (el['@type'] && el['@type'] === 'ListItem' && el.item && el.item.name) return el.item.name;
							return '';
						}).map(s => s ? String(s).trim() : '');
						// find "Trang Chủ" index (case-insensitive)
						const idx = names.findIndex(n => /trang\s*chủ|home/i.test(n));
						if (idx >= 0) {
							const main = names[idx + 1] || '';
							const sub = names[idx + 2] || '';
							return { mainCategory: main, subCategory: sub };
						}
						// fallback: take first two after first element
						if (names.length >= 3) return { mainCategory: names[1] || '', subCategory: names[2] || '' };
					}
				}
			} catch (e) { /* ignore parse errors */ }
		}
	} catch (e) {}

	// Fallback: DOM breadcrumbs
	try {
		const crumbs = [];
		$('[itemprop="itemListElement"]').each((i, el) => {
			const name = $(el).find('[itemprop="name"]').first().text().trim() || $(el).text().trim();
			if (name) crumbs.push(name);
		});
		if (crumbs.length === 0) {
			// common selectors
			['.breadcrumb a', '.breadcrumbs a', 'nav.breadcrumb a', '.product-breadcrumbs a'].forEach(sel => {
				$(sel).each((i, el) => { const t = $(el).text().trim(); if (t) crumbs.push(t); });
			});
		}
		const idx = crumbs.findIndex(n => /trang\s*chủ|home/i.test(n));
		if (idx >= 0) return { mainCategory: crumbs[idx + 1] || '', subCategory: crumbs[idx + 2] || '' };
		if (crumbs.length >= 3) return { mainCategory: crumbs[1] || '', subCategory: crumbs[2] || '' };
	} catch (e) {}

	return { mainCategory: '', subCategory: '' };
}

function extractPrice($, jsonLd) {
	if (jsonLd && jsonLd.offers) {
		const offers = jsonLd.offers;
		if (Array.isArray(offers) && offers.length) return offers[0].price || offers[0].priceSpecification?.price;
		if (offers.price) return offers.price;
	}
	const metaPrice = $('meta[itemprop="price"]').attr('content') || $('meta[property="product:price:amount"]').attr('content');
	if (metaPrice) return metaPrice;
	// fallback to visible price text
	const priceText = $('[class*=price], [id*=price]').first().text() || $('[class*=gia], [class*=price]').first().text();
	if (priceText) return priceText.trim();
	return null;
}

async function parseProduct(url) {
	const html = await fetchHtml(url);
	if (!html) return null;
	const $ = cheerio.load(html);
	const jsonLd = extractLocFromJsonLd($);

	const id = $('meta[itemprop="productID"]').attr('content')
		|| $('meta[property="product:id"]').attr('content')
		|| (jsonLd && (jsonLd.sku || jsonLd.productID))
		|| null;

	const name = $('meta[property="og:title"]').attr('content')
		|| $('meta[name="og:title"]').attr('content')
		|| (jsonLd && jsonLd.name)
		|| $('h1').first().text().trim()
		|| null;

	// remove trailing " | ShopVNB" if present
	let cleanName = name ? String(name).trim() : null;
	if (cleanName) cleanName = cleanName.replace(/\s*\|\s*ShopVNB$/i, '').trim();

	const price = $('meta[itemprop="price"]').attr('content')
		|| extractPrice($, jsonLd)
		|| null;

	const priceCurrency = $('meta[itemprop="priceCurrency"]').attr('content')
		|| (jsonLd && jsonLd.offers && (jsonLd.offers.priceCurrency || (Array.isArray(jsonLd.offers) && jsonLd.offers[0] && jsonLd.offers[0].priceCurrency)))
		|| null;

	const description = $('meta[itemprop="description"]').attr('content')
		|| $('meta[name="description"]').attr('content')
		|| (jsonLd && jsonLd.description)
		|| null;

	// quantity & status: try JSON-LD offers availability or common meta
	let quantity = null;
	let status = null;
	if (jsonLd && jsonLd.offers) {
		const offers = Array.isArray(jsonLd.offers) ? jsonLd.offers[0] : jsonLd.offers;
		if (offers) {
			if (offers.availability) status = offers.availability.replace('http://schema.org/', '').replace('https://schema.org/', '');
			if (offers.inventoryLevel && offers.inventoryLevel.value) quantity = offers.inventoryLevel.value;
		}
	}
	// fallback availability meta
	if (!status) {
		const avail = $('meta[itemprop="availability"]').attr('content') || $('link[itemprop="availability"]').attr('href');
		if (avail) status = String(avail).replace('http://schema.org/', '').replace('https://schema.org/', '');
	}

	let images = extractImages($, jsonLd) || [];
	images = Array.from(new Set(images.map(i => i ? String(i).trim() : '').filter(Boolean)));
	// filter only .webp images as required and exclude theme/logo/system images
	const excludePatterns = [
		'themes_new/images',
		'/templates/images',
		'gio_hang_footer',
		'/logo',
		'/icons/',
		'cdn.shopvnb.com/themes_new',
		'bct.webp'
	];
	images = images.filter(u => /\.webp(\?|$)/i.test(u) && !excludePatterns.some(p => u.toLowerCase().includes(p)));

	const cats = extractCategories($, jsonLd) || { mainCategory: '', subCategory: '' };

	const datePublished = $('meta[itemprop="datePublished"]').attr('content')
		|| (jsonLd && (jsonLd.datePublished || (jsonLd.offers && jsonLd.offers.datePublished)))
		|| '';

	// assign random quantity between 50 and 400; 5% chance sold out
	const rand = Math.random();
	let finalQuantity = '';
	let finalStatus = '';
	if (rand < 0.05) {
		finalQuantity = '0';
		finalStatus = 'hết hàng';
	} else {
		const q = Math.floor(Math.random() * (400 - 50 + 1)) + 50;
		finalQuantity = String(q);
		finalStatus = 'còn hàng';
	}

	const out = {
		id: id ? String(id).trim() : '',
		name: cleanName ? String(cleanName).trim() : '',
		price: price ? String(price).trim() : '',
		priceCurrency: priceCurrency ? String(priceCurrency).trim() : '',
		description: description ? String(description).trim() : '',
		quantity: finalQuantity,
		status: finalStatus,
		images: images,
		mainCategory: cats.mainCategory ? String(cats.mainCategory).trim() : '',
		subCategory: cats.subCategory ? String(cats.subCategory).trim() : '',
		datePublished: datePublished ? String(datePublished).trim() : ''
	};

	return out;
}

async function run() {
	const total = Math.min(products.length, LIMIT || products.length);
	console.log(`Start crawling ${total} products (limit=${LIMIT}, concurrency=${CONCURRENCY})`);
	const results = [];

	for (let i = 0; i < total; i += CONCURRENCY) {
		const batch = products.slice(i, i + CONCURRENCY);
		const jobs = batch.map(p => parseProduct(p.url));
		const res = await Promise.all(jobs);
		res.forEach(r => { if (r) results.push(r); });
		// write incremental
		fs.writeFileSync(OUTPUT_FILE, JSON.stringify(results, null, 2), 'utf8');
		console.log(`Progress: ${Math.min(i + CONCURRENCY, total)}/${total}`);
		await sleep(500);
	}

	console.log(`Done. Wrote ${results.length} items to ${OUTPUT_FILE}`);
}

run().catch(err => { console.error(err); process.exit(1); });
