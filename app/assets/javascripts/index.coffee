$ ->
  wsUrl = $("#dashboard").data("ws-url")
  if wsUrl
    ws = new WebSocket wsUrl
    ws.onmessage = (event) ->
      alert('message from server!')
      message = JSON.parse event.data
      console.log message.type
      switch message.type
        when "data"
          updateDashboard(message)

  updateDashboard = (dashboard) ->
    alert(dashboard)

  incItem = (id) ->
