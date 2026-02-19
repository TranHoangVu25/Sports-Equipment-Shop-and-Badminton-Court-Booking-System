const fs = require('fs');
const path = require('path');

const INPUT_BASE = 'product_detail'; // will try .json
const INPUT_FILE = fs.existsSync(`${INPUT_BASE}.json`) ? `${INPUT_BASE}.json` : INPUT_BASE;
const OUT_MAIN = 'main_category.json';
const OUT_SUB = 'sub_category.json';
const OUT_MAP = 'category.json';

function readInput(file) {
	if (!fs.existsSync(file)) {
		console.error('Input file not found:', file);
		process.exit(1);
	}
	const txt = fs.readFileSync(file, 'utf8');
	try { return JSON.parse(txt); } catch (e) { console.error('Invalid JSON in', file); process.exit(1); }
}

function normalize(s) {
	if (!s && s !== 0) return '';
	return String(s).trim();
}

function uniqueSorted(arr) {
	return Array.from(new Set(arr)).filter(x => x && x.length).sort((a,b)=>a.localeCompare(b,'en',{sensitivity:'base'}));
}

function extract() {
	const items = readInput(INPUT_FILE);
	if (!Array.isArray(items)) { console.error('Expected an array in', INPUT_FILE); process.exit(1); }

	const mainSet = new Set();
	const subSet = new Set();
	const map = Object.create(null);

	for (const it of items) {
		const main = normalize(it.mainCategory || it.main_category || it.main || (it.category && it.category.main) || '');
		const sub = normalize(it.subCategory || it.sub_category || it.sub || (it.category && it.category.sub) || '');

		if (main) mainSet.add(main);
		if (sub) subSet.add(sub);

		if (main) {
			if (!map[main]) map[main] = new Set();
			if (sub) map[main].add(sub);
		}
	}

	const mainList = uniqueSorted(Array.from(mainSet));
	const subList = uniqueSorted(Array.from(subSet));

	const mapOut = {};
	for (const m of mainList) {
		const subs = map[m] ? uniqueSorted(Array.from(map[m])) : [];
		mapOut[m] = subs;
	}

	fs.writeFileSync(OUT_MAIN, JSON.stringify(mainList, null, 2), 'utf8');
	fs.writeFileSync(OUT_SUB, JSON.stringify(subList, null, 2), 'utf8');
	fs.writeFileSync(OUT_MAP, JSON.stringify(mapOut, null, 2), 'utf8');

	console.log('Wrote:', OUT_MAIN, OUT_SUB, OUT_MAP);
	console.log('Main categories:', mainList.length, 'Sub categories:', subList.length);
}

extract();
