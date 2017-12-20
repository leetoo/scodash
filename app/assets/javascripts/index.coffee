$ ->
  wsUrl = $("#dashboard").data("ws-url")
  if wsUrl
    ws = new WebSocket wsUrl
    ws.onmessage = (event) ->
      data = JSON.parse event.data
      console.log data
      initVisibility()
      updateCommas(data)
      updateChart(data)

  initVisibility = () ->
    $("#commas").css "display", "block"
    $("#chart").css "display", "none"


  $('input:radio[name=dashboard-type]').change ->

    $("#commas").css "display", "none"
    $("#chart").css "display", "none"
    type = $(this).data('type')
    if ( type == 'commas-type')
      $("#commas").css "display", "block"
    if ( type == 'chart-type')
      $("#chart").css "display", "block"


  updateCommas = (dashboard) ->
    $("#commas").empty()
    for item in dashboard.items
      itemRow = $("<div>").addClass("item row dashboard-comma-row")
      itemRow.append($("<div>").addClass("col-md-2 vertical-center").text(item.name))
      buttonArea = $("<div>").addClass("col-md-2 buttons-area vertical-center")
      minusBtn = $("<button>").addClass("minus-button").text("-")
      minusBtn.click
        data: {itemId: item.id, hash: dashboard.writeHash}
        handler: (event) ->
          event.preventDefault()
          ws.send(JSON.stringify({operation: 'decrement', itemId:  event.data.itemId, hash: event.data.hash}))
      buttonArea.append(minusBtn)
      buttonArea.append($("<button>").addClass("score-button").text(item.score))
      plusBtn = $("<button>").addClass("plus-button").text("+")
      plusBtn.click
        data: {itemId: item.id, hash: dashboard.writeHash}
        handler: (event) ->
          event.preventDefault()
          ws.send(JSON.stringify({operation: 'increment', itemId:  event.data.itemId, hash: event.data.hash}))

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
    canvas = $("<canvas>").attr("id", "chart-canvas").width(250).height(250)
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
        responsive: false,
        legend: {
          position: "bottom"
        }
      }
    })

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




