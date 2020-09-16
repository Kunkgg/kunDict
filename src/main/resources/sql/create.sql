########################
# Create database
########################
# CREATE DATABASE IF NOT EXISTS mit_10k_dict;

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
  fre_band           char(8)   NOT NULL ,
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
