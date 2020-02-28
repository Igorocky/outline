select
    ROW_NUMBER() over (order by sort_by desc) rn,
    id,
    url,
    has_pgn,
    delay
from (
         select
             id,
             url,
             has_pgn,
             case when millis_remain > 0
                      then MILLIS_TO_DURATION_STR(millis_remain)
                  else trim(TO_CHAR(-millis_remain/planned_delay_millis*100,'999999999'))||'%'
                 end delay,
             case
                 when millis_remain/planned_delay_millis is null then 999999999999999
                 else -millis_remain/planned_delay_millis
                 end sort_by
         from (
                  select
                      id,
                      url,
                      has_pgn,
                      planned_delay_millis,
                      activation_millis,
                      activation_millis - now_millis millis_remain
                  from (
                           select
                               puzzle.ID,
                               url.VALUE url,
                               case when pgn.VALUE is null then 0 else 1 end has_pgn,
                               cast(delay_millis.VALUE as number) planned_delay_millis,
                               STR_INSTANT_TO_MILLIS(activation.VALUE) activation_millis,
                               NOW_MILLIS() now_millis
                           from NODE puzzle
                                    left join TAG paused on puzzle.ID = paused.NODE_ID and paused.TAG_ID = 'chess_puzzle_paused'
                                    left join TAG url on puzzle.ID = url.NODE_ID and url.TAG_ID = 'chess_puzzle_url'
                                    left join TAG pgn on puzzle.ID = pgn.NODE_ID and pgn.TAG_ID = 'chess_puzzle_pgn'
                                    left join TAG delay_millis on puzzle.ID = delay_millis.NODE_ID and delay_millis.TAG_ID = 'chess_puzzle_delay_ms'
                                    left join TAG activation on puzzle.ID = activation.NODE_ID and activation.TAG_ID = 'chess_puzzle_activation'
                           where puzzle.CLAZZ = 'CHESS_PUZZLE' and (paused.VALUE is null or paused.VALUE != 'true')
                       )
              )
)
order by sort_by desc



/*columns
[
  {
    "name": "rn",
    "title": "#"
  },
  {
    "name": "delay",
    "title": "Wait"
  },
  {
    "name": "id",
    "title": "",
    "componentName": "IconButtonReportCmp",
    "componentConfig": {
      "onClickAction":"navigateToPuzzle",
      "iconName":"open_in_new",
      "style": {"color":"grey"},
      "hoverStyle": {"color":"blue"}}
  },
  {
    "name": "url",
    "title": "",
    "renderFunction": "renderStartPuzzle",
    "renderFunctionArgs": ["id", "url", "has_pgn"]
  },
  {
    "name": "urlM",
    "title": "",
    "renderFunction": "renderStartPuzzleM",
    "renderFunctionArgs": ["id", "has_pgn"]
  }
]
columns*/