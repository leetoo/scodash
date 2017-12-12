$ ->
  wsUrl = $("#dashboard").data("ws-url")
  if wsUrl
    ws = new WebSocket wsUrl
    ws.onmessage = (event) ->
      data = JSON.parse event.data
      console.log data
      updateDashboard(data)

  updateDashboard = (dashboard) ->
    $("#dashboard").empty()
    for item in dashboard.items
      itemRow = $("<div>").addClass("item row dashboard-comma-row")
      itemRow.append($("<div>").addClass("col-md-2").text(item.name))
      buttonArea = $("<div>").addClass("col-md-2 buttons-area")
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

      commasArea = $("<div>").addClass("col-md-7")

      scoreFives = item.score // 5
      console.log('scoreFives:' + scoreFives)
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
      $("#dashboard").append(itemRow)



#      @*<div id="@{item.id}" class="item row dashboard-comma-row">*@
#      @*<div class="col-md-2">@{item.name}</div>*@
#                @*<div class="col-md-2 buttons-area">*@
#                    @*<button class="minus-button" onclick="decItem('@{item.id}'')">-</button>*@
#      @*<button class="score-button">@{item.score}</button>*@
#                    @*<button class="plus-button" onclick="incItem('@{item.id}'')">+</button>*@
#      @*</div>*@
#                @*<div class="col-md-7">*@

