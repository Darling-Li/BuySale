import DataTable from './DataTable.vue'
import MetricCard from './MetricCard.vue'
import MetricGrid from './MetricGrid.vue'
import ResultMessage from './ResultMessage.vue'
import UiButton from './UiButton.vue'
import UiField from './UiField.vue'
import UiPanel from './UiPanel.vue'
import UiTag from './UiTag.vue'
import UiToolbar from './UiToolbar.vue'

const components = {
  DataTable,
  MetricCard,
  MetricGrid,
  ResultMessage,
  UiButton,
  UiField,
  UiPanel,
  UiTag,
  UiToolbar
}

export function installUi(app) {
  Object.entries(components).forEach(([name, component]) => {
    app.component(name, component)
  })
}
