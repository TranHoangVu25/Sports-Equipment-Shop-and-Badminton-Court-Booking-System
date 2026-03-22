const fs = require('fs');
const path = require('path');

const DEFAULT_FILE = 'product_detail.json';

function parsePriceToVnd(value) {
  if (value === undefined || value === null) {
    return null;
  }

  if (typeof value === 'number') {
    return Number.isFinite(value) ? value : null;
  }

  const normalized = String(value)
    .trim()
    .toLowerCase()
    .replace(/vnd/g, '')
    .replace(/[đd]/g, '')
    .replace(/[^\d]/g, '');

  if (normalized === '') {
    return null;
  }

  const parsed = Number.parseInt(normalized, 10);
  return Number.isFinite(parsed) ? parsed : null;
}

function isZeroPriceProduct(product) {
  return parsePriceToVnd(product && product.price) === 0;
}

function run() {
  const inputPath = process.argv[2]
    ? path.resolve(process.argv[2])
    : path.resolve(__dirname, DEFAULT_FILE);

  const raw = fs.readFileSync(inputPath, 'utf8').replace(/^\uFEFF/, '');
  const data = JSON.parse(raw);

  if (!Array.isArray(data)) {
    throw new Error('File JSON phai la mang san pham.');
  }

  const removedSamples = [];
  const filtered = data.filter((product) => {
    const shouldRemove = isZeroPriceProduct(product);
    if (shouldRemove && removedSamples.length < 10) {
      removedSamples.push({
        id:
          product && product.id !== undefined && product.id !== null
            ? String(product.id)
            : '',
        price:
          product && product.price !== undefined && product.price !== null
            ? String(product.price)
            : '',
        name:
          product && product.name !== undefined && product.name !== null
            ? String(product.name)
            : '',
      });
    }
    return !shouldRemove;
  });

  const removedCount = data.length - filtered.length;

  fs.writeFileSync(inputPath, `${JSON.stringify(filtered, null, 2)}\n`, 'utf8');

  console.log(`Tong truoc: ${data.length}`);
  console.log(`Da xoa gia 0: ${removedCount}`);
  console.log(`Tong sau: ${filtered.length}`);
  console.log(`File da ghi: ${inputPath}`);

  if (removedSamples.length > 0) {
    console.log('Mau san pham da xoa (toi da 10):');
    for (const sample of removedSamples) {
      console.log(`- [${sample.id}] ${sample.price} | ${sample.name}`);
    }
  }
}

run();
