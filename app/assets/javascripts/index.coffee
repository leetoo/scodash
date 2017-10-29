$ ->
  wsUrl = $("#dashboard").data("ws-url")
  if wsUrl
    ws = new WebSocket wsUrl
    ws.onmessage = (event) ->
      alert('message from server!')
      data = JSON.parse event.data
      console.log data
      updateDashboard(data)

  updateDashboard = (dashboard) ->
    $("#dashboard").empty()
    for item in dashboard.items
      itemRow = $("<div>").addClass("item row dashboard-comma-row")
      itemRow.append($("<div>").addClass("col-md-2").text(item.name))
      buttonArea = $("<div>").addClass("col-md-2 buttons-area")
      buttonArea.append($("<button>").addClass("minus-button").text("-"))
      buttonArea.append($("<button>").addClass("score-button").text(item.score))
      buttonArea.append($("<button>").addClass("plus-button").text("+"))
      itemRow.append(buttonArea)
      itemRow.append($("<div>").addClass("col-md-7"))
      $("#dashboard").append(itemRow)



#      @*<div id="@{item.id}" class="item row dashboard-comma-row">*@
#      @*<div class="col-md-2">@{item.name}</div>*@
#                @*<div class="col-md-2 buttons-area">*@
#                    @*<button class="minus-button" onclick="decItem('@{item.id}'')">-</button>*@
#      @*<button class="score-button">@{item.score}</button>*@
#                    @*<button class="plus-button" onclick="incItem('@{item.id}'')">+</button>*@
#      @*</div>*@
#                @*<div class="col-md-7">*@

