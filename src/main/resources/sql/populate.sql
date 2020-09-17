##########################
# insert water word
##########################

INSERT INTO frequencies(fre_band, fre_description)
VALUES(5, 'Extremely Common. water is one of the 1000 most commonly used words in the Collins dictionary');

INSERT INTO words(word_spell, word_source, word_forms, word_pron_soundmark, word_pron_sound, fre_id)
VALUES('water', 'Collins Online English Dictionary', '[waters, watering, watered]', 'wɔtər', 'http://test.com/sound.mp3', 1);

INSERT INTO entries(entry_wordClass, entry_sense, word_id)
VALUES('uncountable noun', 'Water is a clear thin liquid that has no color or taste when it is pure. It falls from clouds as rain and enters rivers and seas. All animals and people need water in order to live.', 1);

INSERT INTO entries(entry_wordClass, entry_sense, word_id)
VALUES('plural noun', 'You use waters to refer to a large area of sea, especially the area of sea that is near to a country and that is regarded as belonging to it.', 1);

INSERT INTO entries(entry_wordClass, entry_sense, word_id)
VALUES('transitive verb', 'If you water plants, you pour water over them in order to help them to grow.', 1);

INSERT INTO entries(entry_wordClass, entry_sense, word_id)
VALUES('intransitive verb', 'If your eyes water, tears build up in them because they are hurting or because you are upset.', 1);

INSERT INTO entries(entry_wordClass, entry_sense, word_id)
VALUES('intransitive verb', 'If you say that your mouth is watering, you mean that you can smell or see some nice food that makes you want to eat it.', 1);

INSERT INTO examples(example_text, entry_id)
VALUES('Get me a glass of water.', 1);

INSERT INTO examples(example_text, entry_id)
VALUES('...the sound of water hammering on the metal roof.', 1);

INSERT INTO examples(example_text, entry_id)
VALUES('The ship will remain outside Chinese territorial waters.', 2);

INSERT INTO examples(example_text, entry_id)
VALUES('He went out to water the plants.', 3);

INSERT INTO examples(example_text, entry_id)
VALUES('His eyes watered from cigarette smoke.', 4);

INSERT INTO examples(example_text, entry_id)
VALUES('...cookies to make your mouth water.', 5);


SELECT word_spell, word_source, word_forms,word_pron_soundmark, word_pron_sound, fre_band,fre_description, entry_wordClass, entry_sense, example_text FROM words, frequencies, entries, examples WHERE (words.fre_id = frequencies.fre_id AND words.word_id = entries.word_id AND entries.entry_id = examples.entry_id AND words.word_spell = 'water')
