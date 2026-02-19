const axios = require('axios');
const fs = require('fs');

const SITEMAP_INDEX_URL = 'https://shopvnb.com/sitemaps.xml';

async function fetchText(url) {
  const res = await axios.get(url, {
    headers: { 'User-Agent': 'Mozilla/5.0' },
    timeout: 20000
  });
  return res.data;
}

function extractLocs(xml) {
  const locs = [];
  const re = /<loc>\s*([^<]+?)\s*<\/loc>/gi;
  let m;
  while ((m = re.exec(xml)) !== null) locs.push(m[1]);
  return locs;
}

async function crawlProductLinks() {
  console.log('🔍 Đang đọc sitemap index...');
  const indexXml = await fetchText(SITEMAP_INDEX_URL);

  const allSitemaps = extractLocs(indexXml);
  const productSitemaps = allSitemaps.filter(u => u.includes('/sitemap/san-pham/'));

  console.log(`📦 Tổng sitemap sản phẩm: ${productSitemaps.length}`);

  const productSet = new Set();

  for (const sitemapUrl of productSitemaps) {
    console.log('➡️ Crawl:', sitemapUrl);
    try {
      const xml = await fetchText(sitemapUrl);
      const locs = extractLocs(xml);
      for (const l of locs) productSet.add(l);
    } catch (err) {
      console.error('❌ Lỗi khi crawl sitemap:', sitemapUrl, err.message);
    }
  }

  const productLinks = Array.from(productSet).map(url => ({ url }));
  fs.writeFileSync('products.json', JSON.stringify(productLinks, null, 2), 'utf-8');

  console.log(`✅ Hoàn tất! Tổng sản phẩm: ${productLinks.length}`);
}

crawlProductLinks().catch(err => {
  console.error(err);
  process.exit(1);
});
