export const fallbackUnitOptions = [
  { label: '斤', unitToJin: 1 },
  { label: '吨', unitToJin: 2000 },
  { label: 'kg', unitToJin: 2 }
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

export function unitPriceFromJinPrice(priceInput, unitToJin, priceMode = 'YUAN_PER_JIN') {
  const factor = Number(unitToJin || 0)
  const price = Number(priceInput || 0)
  const pricePerJin = priceMode === 'MAO_PER_JIN' ? price / 10 : price
  return pricePerJin * factor
}

export function unitOptionFromResponse(item) {
  return {
    label: item.name,
    unitToJin: Number(item.unitToJin || 0),
    systemBuiltin: item.systemBuiltin
  }
}

export function defaultUnit(options = fallbackUnitOptions) {
  return options[0] || fallbackUnitOptions[0]
}

export function unitByLabel(label, options = fallbackUnitOptions) {
  return options.find((item) => item.label === label) || defaultUnit(options)
}
