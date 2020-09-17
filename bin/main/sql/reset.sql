########################
# Drop database
########################
DROP DATABASE mit_10k_dict;
########################
# Create database
########################
CREATE DATABASE IF NOT EXISTS mit_10k_dict;
USE mit_10k_dict;

########################
# Create words table
########################
CREATE TABLE words
(
  word_id                int       NOT NULL AUTO_INCREMENT,
  word_spell             char(50)  NOT NULL ,
  word_source            char(50)  NULL ,
  word_forms             char(255) NULL ,
  word_pron_soundmark    char(50)  NULL ,
  word_pron_sound        char(255) NULL ,
  fre_id                 int       NULL ,
  PRIMARY KEY (word_id)
) ENGINE=InnoDB;

########################
# Create frequencies table
########################
CREATE TABLE frequencies
(
  fre_id             int       NOT NULL AUTO_INCREMENT,
  fre_band           int   NOT NULL ,
  fre_description    text      NULL ,
  PRIMARY KEY(fre_id)
) ENGINE=InnoDB;

########################
# Create entries table
########################
CREATE TABLE entries
(
  entry_id         int       NOT NULL AUTO_INCREMENT,
  entry_wordClass  char(255) NOT NULL ,
  entry_sense      text      NULL ,
  word_id          int       NULL ,
  PRIMARY KEY(entry_id)
) ENGINE=InnoDB;

########################
# Create examples table
########################
CREATE TABLE examples
(
  example_id      int       NOT NULL AUTO_INCREMENT,
  example_text    text      NOT NULL ,
  entry_id        int       NOT NULL ,
  PRIMARY KEY(example_id),
  FULLTEXT(example_text)
) ENGINE=MyISAM;

ALTER TABLE words ADD CONSTRAINT fk_words_frequencies FOREIGN KEY (fre_id) REFERENCES  frequencies (fre_id);
ALTER TABLE entries ADD CONSTRAINT fk_entries_words FOREIGN KEY (word_id) REFERENCES  words (word_id);
ALTER TABLE examples ADD CONSTRAINT fk_examples_entries FOREIGN KEY (entry_id) REFERENCES  entries (entry_id);

CREATE UNIQUE INDEX index_word ON words (word_spell)
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
