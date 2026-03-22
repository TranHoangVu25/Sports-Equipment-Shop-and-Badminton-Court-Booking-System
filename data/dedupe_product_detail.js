const fs = require('fs');
const path = require('path');

const DEFAULT_FILE = 'product_detail.json';

function normalizeId(item) {
  if (!item || item.id === undefined || item.id === null) {
    return '';
  }
  return String(item.id).trim();
}

function dedupeById(items) {
  const seen = new Set();
  const result = [];
  let removed = 0;

  for (const item of items) {
    const id = normalizeId(item);

    if (id === '') {
      result.push(item);
      continue;
    }

    if (seen.has(id)) {
      removed += 1;
      continue;
    }

    seen.add(id);
    result.push(item);
  }

  return { result, removed };
}

function run() {
  const inputPath = process.argv[2]
    ? path.resolve(process.argv[2])
    : path.resolve(__dirname, DEFAULT_FILE);

  const raw = fs.readFileSync(inputPath, 'utf8');
  const data = JSON.parse(raw);

  if (!Array.isArray(data)) {
    throw new Error('File JSON phai la mang object.');
  }

  const before = data.length;
  const { result, removed } = dedupeById(data);

  fs.writeFileSync(inputPath, `${JSON.stringify(result, null, 2)}\n`, 'utf8');

  console.log(`Tong truoc: ${before}`);
  console.log(`Da xoa trung id: ${removed}`);
  console.log(`Tong sau: ${result.length}`);
  console.log(`File da ghi: ${inputPath}`);
}

run();
