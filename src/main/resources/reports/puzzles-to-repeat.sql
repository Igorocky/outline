select
    id,
    url,
    case when millis_remain > 0
        then MILLIS_TO_DURATION_STR(millis_remain)
        else TO_CHAR(-millis_remain/planned_delay_millis*100,'999999999')||'%'
    end delay
from (
    select
        id,
        url,
        planned_delay_millis,
        activation_millis,
        activation_millis - now_millis millis_remain
    from (
        select
            puzzle.ID,
            url.VALUE url,
            cast(delay_millis.VALUE as number) planned_delay_millis,
            STR_INSTANT_TO_MILLIS(activation.VALUE) activation_millis,
            NOW_MILLIS() now_millis
        from NODE puzzle
            left join TAG paused on puzzle.ID = paused.NODE_ID and paused.TAG_ID = 'chess_puzzle_paused'
            left join TAG url on puzzle.ID = url.NODE_ID and url.TAG_ID = 'chess_puzzle_url'
            left join TAG delay_millis on puzzle.ID = delay_millis.NODE_ID and delay_millis.TAG_ID = 'chess_puzzle_delay_ms'
            left join TAG activation on puzzle.ID = activation.NODE_ID and activation.TAG_ID = 'chess_puzzle_activation'
        where puzzle.CLAZZ = 'CHESS_PUZZLE' and (paused.VALUE is null or paused.VALUE != 'true')
    )
)
order by -millis_remain/planned_delay_millis desc



/*columns
[

]
columns*/