select a.ID, a.CREATED_WHEN, passed.VALUE PASSED, delay.VALUE DELAY
from NODE p
         inner join NODE a on p.ID = a.PARENT_NODE_ID and a.CLAZZ = 'CHESS_PUZZLE_ATTEMPT'
         left join TAG passed on a.ID = passed.NODE_ID and passed.TAG_ID = 'chess_puzzle_passed'
         left join TAG delay on a.ID = delay.NODE_ID and delay.TAG_ID = 'chess_puzzle_delay'
where p.ID = :puzzleId
order by a.CREATED_WHEN desc

/*columns
[
  {
    "name": "ID",
    "title": "",
    "componentName": "IconButtonReportCmp",
    "componentConfig": {
        "onClickAction":"deleteHistoryRecord",
        "iconName":"delete_forever",
        "style": {"color":"grey"},
        "hoverStyle": {"color":"red"}}
  },
  {
    "name": "CREATED_WHEN",
    "title": "Created",
    "componentName": "TimestampFromInstant"
  },
  {
    "name": "PASSED",
    "title": "Result",
    "componentName": "BooleanPassFail",
    "componentConfig": {"style": {"fontSize": "20px"}}
  },
  {
    "name": "DELAY",
    "title": "Delay"
  }
]
columns*/

