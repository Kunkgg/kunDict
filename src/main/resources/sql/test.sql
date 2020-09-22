##########################
# Alias
##########################
SELECT word_id `ID`, word_spell `spell`
FROM mit10k_words
WHERE word_id > 3
ORDER BY `ID` DESC;


SELECT word_id `ID`, word_id id, word_spell `spell`, fre_band `band`
FROM mit10k_words w, mit10k_frequencies f
WHERE w.fre_id = f.fre_id
ORDER BY `ID`;

##########################
# INNER JOIN
##########################
select * from members m INNER JOIN committees c USING (name);
select * from members m INNER JOIN committees c ON m.name = c.name;

##########################
# LEFT JOIN
##########################
select * from members m LEFT JOIN committees c ON m.name = c.name;

##########################
# CROSS JOIN
# does not have a join condition
##########################
SELECT
    m.member_id,
    m.name member,
    c.committee_id,
    c.name committee
FROM
    members m
CROSS JOIN committees c;


##########################
# Tips to find tables affected by MySQL ON DELETE CASCADE action
##########################
USE information_schema;
SELECT
    table_name
FROM
    referential_constraints
WHERE
    constraint_schema = 'dict'
        AND referenced_table_name = 'word'
        AND delete_rule = 'CASCADE'
