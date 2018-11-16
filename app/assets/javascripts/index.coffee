window.currentDashboardType = 'COMMA'

window.initRecent = () ->
  $("#recent-dashboards-section").css "display", "none"
  $("#monitor-preview-section").css "display", "none"
  hashes = localStorage.getItem("hashes");
  if hashes == null
    hashesArr = []
    $("#monitor-preview-section").css "display", "block"
  else
    hashesArr = JSON.parse(hashes)
    $.ajax '/dashboardsData/' + hashesArr,
      type: 'GET'
      success: (data) ->
        dashboards = JSON.parse(data)
        i = 0
        for dashboard in dashboards
          hash = dashboard.readonlyHash
          suffix = "(readonly)"
          if !!dashboard.writeHash
            hash = dashboard.writeHash
            suffix = ""
          anchor = $("<a>", {href: window.location.href + "dashboard/" + hash}).html(dashboard.name + " - " + dashboard.description + " " + suffix)
          div = $("<div>").append(anchor)
          $("#recent-dashboards").append(div)
        if dashboards.length > 0
          $("#recent-dashboards-section").css "display", "block"
          $("#monitor-preview-section").css "display", "none"
        else
          $("#recent-dashboards-section").css "display", "none"
          $("#monitor-preview-section").css "display", "block"




window.initSorting = () ->
  sorting = $("#sorting")
  sorting.change
    data: {hash: sorting.data('hash')}
    handler: (event) ->
      event.preventDefault()
      ws.send(JSON.stringify({operation: 'sort', hash: event.data.hash, sorting: this.value}))


$ ->
  initRecent()
  initSorting()
  wsUrl = $("#dashboard").data("ws-url")
  if wsUrl
    window.ws = new WebSocket wsUrl
    window.ws.onmessage = (event) ->
      data = JSON.parse event.data
      console.log data
      initVisibility()
      updateCommas(data)
      updateChart(data)
      updateDate(data)
    window.ws.onclose = (event) ->
      console.log 'ws closed'

  initVisibility = () ->
    if (window.currentDashboardType == 'COMMA')
      $("#commas").css "display", "block"
      $("#chart").css "display", "none"
    else
      $("#commas").css "display", "none"
      $("#chart").css "display", "block"



  $('input:radio[name=dashboard-type]').change ->

    $("#commas").css "display", "none"
    $("#chart").css "display", "none"
    type = $(this).data('type')
    if ( type == 'commas-type')
      $("#commas").css "display", "block"
      window.currentDashboardType = 'COMMA'
    if ( type == 'chart-type')
      $("#chart").css "display", "block"
      window.currentDashboardType = 'CHART'


  updateCommas = (dashboard) ->
    $("#commas").empty()
    for item in dashboard.items
      itemRow = $("<div>").addClass("item row dashboard-comma-row")
      itemRow.append($("<div>").addClass("col-md-2 vertical-center").attr("style", "word-break:break-word").text(item.name))

      buttonArea = $("<div>").addClass("col-md-2 buttons-area vertical-center")

      if !!dashboard.writeHash
        minusBtn = $("<button>").addClass("minus-button").text("-")
        minusBtn.click
          data: {itemId: item.id, hash: dashboard.writeHash}
          handler: (event) ->
            event.preventDefault()
            ws.send(JSON.stringify({operation: 'decrement', itemId:  event.data.itemId, hash: event.data.hash, tzOffset: new Date().getTimezoneOffset()}))
        buttonArea.append(minusBtn)

      buttonArea.append($("<button>").addClass("score-button").text(item.score))

      if !!dashboard.writeHash
        plusBtn = $("<button>").addClass("plus-button").text("+")
        plusBtn.click
          data: {itemId: item.id, hash: dashboard.writeHash}
          handler: (event) ->
            event.preventDefault()
            ws.send(JSON.stringify({operation: 'increment', itemId:  event.data.itemId, hash: event.data.hash, tzOffset: new Date().getTimezoneOffset() }))

        buttonArea.append(plusBtn)

      itemRow.append(buttonArea)

      commasArea = $("<div>").addClass("col-md-7 vertical-center")

      scoreFives = item.score // 5
      for fives in [1 .. scoreFives] by 1
        commasArea.append($("<img>").attr('src', '/assets/images/5-carek.png'))

      img = $("<img>")
      scoreRemain = item.score % 5
      switch scoreRemain
        when 1 then img.attr('src', '/assets/images/1-carka.png')
        when 2 then img.attr('src', '/assets/images/2-carky.png')
        when 3 then img.attr('src', '/assets/images/3-carky.png')
        when 4 then img.attr('src', '/assets/images/4-carky.png')
      commasArea.append(img)

      itemRow.append(commasArea)
      $("#commas").append(itemRow)

  updateChart = (dashboard) ->
    $("#chart-canvas").remove()
    canvas = $("<canvas>")
      .attr("id", "chart-canvas")
#      .attr("width", 500)
#      .attr("height", 500)
    $("#chart").append(canvas)

    scoreArray = []
    colorArray = []
    nameArray = []
    for item, index in dashboard.items
      scoreArray[index] = item.score
      colorArray[index] = randomColors[index]
      nameArray[index] = item.name
    ctx = $("#chart-canvas")
    chart = new Chart(ctx, {
      type: 'doughnut',
      data: {
        datasets: [{
          data: scoreArray,
          backgroundColor: colorArray
        }],
        labels: nameArray
      },
      options: {
        animation: {
          duration: 0
        },
        responsive: true,
        legend: {
          position: "bottom"
        }
      }
    })

  updateDate = (dashboard) ->
    updatedDate = new Date(dashboard.updated)
    $("#updated").text((updatedDate.toLocaleString()))
    createdDate = new Date(dashboard.created)
    $("#created").text((createdDate.toLocaleDateString()))


randomColors = [
  '#FF6633',
  '#3366E6',
  '#80B300',
  '#E6B333',
  '#FF33FF',
  '#B33300',
  '#FFB399',
  '#E6331A',
  '#99FF99',
  '#00B3E6',
  '#CCCC00',
  '#B34D4D',
  '#809900',
  '#999966',
  '#E6B3B3',
  '#6680B3',
  '#FFFF99',
  '#FF3380',
  '#66991A',
  '#FF99E6',
  '#CCFF1A',
  '#FF1A66',
  '#33FFCC',
  '#66994D',
  '#B366CC',
  '#4D8000',
  '#CC80CC',
  '#66664D',
  '#991AFF',
  '#E666FF',
  '#4DB3FF',
  '#1AB399',
  '#E666B3',
  '#33991A',
  '#CC9999',
  '#B3B31A',
  '#00E680',
  '#4D8066',
  '#809980',
  '#E6FF80',
  '#6666FF'
  '#1AFF33',
  '#999933',
  '#66E64D',
  '#4D80CC',
  '#9900B3',
  '#E64D66',
  '#4DB380',
  '#FF4D4D',
  '#99E6E6',
]




