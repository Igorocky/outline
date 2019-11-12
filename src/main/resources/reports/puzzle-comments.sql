select comment from (
    select value comment, rand() ord
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