export const unitOptions = [
  { label: '斤', unitToJin: 1 },
  { label: '公斤', unitToJin: 2 },
  { label: '吨', unitToJin: 2000 },
  { label: '袋', unitToJin: 100 }
]

export function convertedWeightJin(quantity, unitToJin) {
  return Number(quantity || 0) * Number(unitToJin || 0)
}

export function convertedPricePerJin(unitPrice, unitToJin) {
  const factor = Number(unitToJin || 0)
  if (factor <= 0) {
    return 0
  }
  return Number(unitPrice || 0) / factor
}

export function defaultUnit() {
  return unitOptions[0]
}

export function unitByLabel(label) {
  return unitOptions.find((item) => item.label === label) || defaultUnit()
}
