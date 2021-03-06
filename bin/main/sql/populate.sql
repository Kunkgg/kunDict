##########################
# insert Word("water")
##########################

INSERT INTO mit10k_frequencies(fre_band, fre_description)
VALUES(5, 'Extremely Common. water is one of the 1000 most commonly used words in the Collins dictionary');

INSERT INTO mit10k_words(word_spell, word_source, word_forms, word_pron_soundmark, word_pron_sound, fre_id)
VALUES('water', 'Collins Online English Dictionary', '[waters, watering, watered]', 'wɔtər', 'https://www.collinsdictionary.com/us/sounds/hwd_sounds/en_us_water_2.mp3', 1);

INSERT INTO mit10k_entries(entry_wordClass, entry_sense, word_id)
VALUES('uncountable noun', 'Water is a clear thin liquid that has no color or taste when it is pure. It falls from clouds as rain and enters rivers and seas. All animals and people need water in order to live.', 1);

INSERT INTO mit10k_entries(entry_wordClass, entry_sense, word_id)
VALUES('plural noun', 'You use waters to refer to a large area of sea, especially the area of sea that is near to a country and that is regarded as belonging to it.', 1);

INSERT INTO mit10k_entries(entry_wordClass, entry_sense, word_id)
VALUES('transitive verb', 'If you water plants, you pour water over them in order to help them to grow.', 1);

INSERT INTO mit10k_entries(entry_wordClass, entry_sense, word_id)
VALUES('intransitive verb', 'If your eyes water, tears build up in them because they are hurting or because you are upset.', 1);

INSERT INTO mit10k_entries(entry_wordClass, entry_sense, word_id)
VALUES('intransitive verb', 'If you say that your mouth is watering, you mean that you can smell or see some nice food that makes you want to eat it.', 1);

INSERT INTO mit10k_examples(example_text, entry_id)
VALUES('Get me a glass of water.', 1);

INSERT INTO mit10k_examples(example_text, entry_id)
VALUES('...the sound of water hammering on the metal roof.', 1);

INSERT INTO mit10k_examples(example_text, entry_id)
VALUES('The ship will remain outside Chinese territorial waters.', 2);

INSERT INTO mit10k_examples(example_text, entry_id)
VALUES('He went out to water the plants.', 3);

INSERT INTO mit10k_examples(example_text, entry_id)
VALUES('His eyes watered from cigarette smoke.', 4);

INSERT INTO mit10k_examples(example_text, entry_id)
VALUES('...cookies to make your mouth water.', 5);

# Example for query a word from database:
# SELECT word_spell, word_source, word_forms,word_pron_soundmark, word_pron_sound, fre_band,fre_description, entry_wordClass, entry_sense, example_text FROM words, frequencies, entries, examples WHERE (words.fre_id = frequencies.fre_id AND words.word_id = entries.word_id AND entries.entry_id = examples.entry_id AND words.word_spell = 'water')


##########################
# insert Word("ace")
##########################

INSERT INTO mit10k_frequencies(fre_band, fre_description)
VALUES(2, 'Used Occasionally. ace is one of the 30000 most commonly used words in the Collins dictionary');

INSERT INTO mit10k_words(word_spell, word_source, word_forms, word_pron_soundmark, word_pron_sound, fre_id)
VALUES('ace', 'Collins Online English Dictionary', '[aces]', 'eɪs', 'https://www.collinsdictionary.com/us/sounds/hwd_sounds/en_us_ace_1.mp3', 2);

INSERT INTO mit10k_entries(entry_wordClass, entry_sense, word_id)
VALUES('countable noun', 'An ace is a playing card with a single symbol on it. In most card games, the ace of a particular suit has either the highest or the lowest value of the cards in that suit.', 2);

INSERT INTO mit10k_entries(entry_wordClass, entry_sense, word_id)
VALUES('countable noun', 'If you describe someone such as a sports player as an ace, you mean that they are very good at what they do.', 2);

INSERT INTO mit10k_entries(entry_wordClass, entry_sense, word_id)
VALUES('countable noun', 'In tennis, an ace is a serve which is so fast that the other player cannot reach the ball.', 2);

INSERT INTO mit10k_examples(example_text, entry_id)
VALUES('...the ace of hearts.', 6);

INSERT INTO mit10k_examples(example_text, entry_id)
VALUES('Despite the loss of their ace early in the game, Seattle beat the Brewers 6-5.', 7);

INSERT INTO mit10k_examples(example_text, entry_id)
VALUES('Agassi believed he had served an ace at 5-3 (40-30) in the deciding set.', 8);

##########################
# insert dict_types table
##########################
INSERT INTO dict_types(dict_type)
VALUES('Online');

INSERT INTO dict_types(dict_type)
VALUES('Local');

##########################
# insert dicts table
##########################
INSERT INTO dicts(dict_name, dict_shorName, dict_type_id, dict_size, dict_dbName, dict_mtime, dict_atime)
VALUES('MIT 10K Englinsh Dictionary', 1, 'mit10k');

INSERT INTO dicts(dict_name, dict_type_id)
VALUES('Collins Online English Dictionary', 2);

##########################
# delete a word
##########################
DELETE  mit10k_examples,mit10k_entries, mit10k_words
FROM mit10k_words, mit10k_entries, mit10k_examples
WHERE (mit10k_words.word_id = mit10k_entries.word_id AND mit10k_entries.entry_id = mit10k_examples.entry_id AND mit10k_words.word_spell = 'ace')
