/*columns
[
{"name":"CREATED_WHEN", "title":"Created", "componentName":"TimestampFromInstant"}
,{"name":"PASSED", "title":"Result", "componentName":"BooleanPassFail"}
]
columns*/

select a.CREATED_WHEN, passed.VALUE PASSED
from NODE p
    left join NODE a on p.ID = a.PARENT_NODE_ID and a.CLAZZ = 'CHESS_PUZZLE_ATTEMPT'
    left join TAG passed on a.ID = passed.NODE_ID and passed.TAG_ID = 'chess_puzzle_passed'
where p.ID = :puzzleId
order by a.CREATED_WHEN desc