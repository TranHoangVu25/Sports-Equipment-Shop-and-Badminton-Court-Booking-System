const fs = require('fs');
const path = require('path');

const DEFAULT_FILE = 'product_detail.json';

function toInt(value) {
  const num = Number.parseInt(value, 10);
  return Number.isFinite(num) && num > 0 ? num : 0;
}

function normalizeText(text) {
  if (typeof text !== 'string') {
    return '';
  }

  return text
    .toLowerCase()
    .normalize('NFD')
    .replace(/[\u0300-\u036f]/g, '')
    .replace(/d/g, 'd')
    .replace(/[^a-z0-9]+/g, ' ')
    .trim();
}

function isOutOfStockStatus(status) {
  const normalized = normalizeText(status);

  return (
    normalized.includes('het hang') ||
    normalized.includes('out of stock') ||
    normalized.includes('sold out')
  );
}

function distributeByRatio(values, targetTotal) {
  const sum = values.reduce((acc, value) => acc + value, 0);

  if (targetTotal <= 0) {
    return values.map(() => 0);
  }

  if (sum <= 0) {
    return values.map((_, index) => (index === 0 ? targetTotal : 0));
  }

  const scaled = values.map((value) => (value * targetTotal) / sum);
  const base = scaled.map((value) => Math.floor(value));
  let remainder = targetTotal - base.reduce((acc, value) => acc + value, 0);

  const indicesByFraction = scaled
    .map((value, index) => ({ index, fraction: value - Math.floor(value) }))
    .sort((a, b) => b.fraction - a.fraction);

  for (let i = 0; i < indicesByFraction.length && remainder > 0; i += 1) {
    base[indicesByFraction[i].index] += 1;
    remainder -= 1;
  }

  return base;
}

function syncProductSizeQuantities(product) {
  if (!product || !Array.isArray(product.size) || product.size.length === 0) {
    return { changed: false, zeroed: false, balanced: false };
  }

  const outsideQuantity = toInt(product.quantity);
  const outsideStatusIsOut = isOutOfStockStatus(product.status);
  const current = product.size.map((item) => toInt(item && item.quantity));

  let next = current;
  let zeroed = false;
  let balanced = false;

  if (outsideQuantity === 0 && outsideStatusIsOut) {
    next = current.map(() => 0);
    zeroed = true;
  } else {
    const currentSum = current.reduce((acc, value) => acc + value, 0);
    if (currentSum !== outsideQuantity) {
      next = distributeByRatio(current, outsideQuantity);
      balanced = true;
    }
  }

  let changed = false;
  for (let i = 0; i < product.size.length; i += 1) {
    if (product.size[i].quantity !== next[i]) {
      product.size[i].quantity = next[i];
      changed = true;
    }
  }

  return { changed, zeroed, balanced };
}

function run() {
  const inputPath = process.argv[2]
    ? path.resolve(process.argv[2])
    : path.resolve(__dirname, DEFAULT_FILE);

  const raw = fs.readFileSync(inputPath, 'utf8');
  const data = JSON.parse(raw);

  if (!Array.isArray(data)) {
    throw new Error('File JSON phai la mang san pham.');
  }

  let changedProducts = 0;
  let zeroedProducts = 0;
  let balancedProducts = 0;

  for (const product of data) {
    const result = syncProductSizeQuantities(product);
    if (result.changed) {
      changedProducts += 1;
    }
    if (result.zeroed) {
      zeroedProducts += 1;
    }
    if (result.balanced) {
      balancedProducts += 1;
    }
  }

  fs.writeFileSync(inputPath, `${JSON.stringify(data, null, 2)}\n`, 'utf8');

  console.log(`Da cap nhat: ${changedProducts} san pham.`);
  console.log(`- Dua ve 0 theo status het hang: ${zeroedProducts}`);
  console.log(`- Can bang tong size theo quantity ngoai: ${balancedProducts}`);
  console.log(`File da ghi: ${inputPath}`);
}

run();
