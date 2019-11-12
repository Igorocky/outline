select comment from (
    select value comment,
    case when REGEXP_LIKE(value, '^([prnbkqPRNBKQ1-8]{1,8}/){7}([prnbkqPRNBKQ1-8]{1,8}) [wb] [-KQkq]{1,4} [-a-h1-8]{1,2} \d+ \d+$') then 2 else rand() end ord
    from tag
    where tag_id = 'chess_puzzle_comment_text'
)
order by ord

/*columns
[
  {
    "name": "comment",
    "title": "Comment"
  }
]
columns*/