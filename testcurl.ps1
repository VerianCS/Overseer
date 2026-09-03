$json = @'
[
  {
    "playerId": "c446a8b7-8461-420a-8bf8-02456e7ff2b3",
    "playerName": "Alex",
    "world": "world",
    "x": 142,
    "y": -58,
    "z": -310,
    "blockType": "DEEPSLATE_DIAMOND_ORE",
    "toolUsed": "DIAMOND_PICKAXE",
    "toolEfficiencyLevel": 4,
    "hasHaste": false,
    "hasMiningFatigue": false,
    "isExposedToAirOrCave": false,
    "breakDeltaMs": 180,
    "timestampMs": 1725296400000
  }
]
'@

Invoke-RestMethod -Uri "http://localhost:8085/telemetry" -Method Post -Body $json -ContentType "application/json"