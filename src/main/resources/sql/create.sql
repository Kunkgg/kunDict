########################
# Create database
########################
CREATE DATABASE IF NOT EXISTS dict;
USE dict;

/* Top level tables {{{ */
########################
# Create dicts table
########################
CREATE TABLE dicts
(
  dict_id                int       NOT NULL AUTO_INCREMENT,
  dict_name              char(50)  NOT NULL UNIQUE,
  dict_type_id           int       NULL ,
  dict_dbName            char(50)  NULL UNIQUE,
  dict_size              int       DEFAULT 0,
  dict_mtime             TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  dict_atime             TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (dict_id)
) ENGINE=InnoDB;


########################
# Create dict_types table
########################
CREATE TABLE dict_types
(
  dict_type_id           int       NOT NULL AUTO_INCREMENT,
  dict_type              char(50)  NULL UNIQUE,
  PRIMARY KEY (dict_type_id)
) ENGINE=InnoDB;

/* }}} Top level */

/* Each dict tables {{{ */
########################
# Create words table
########################
CREATE TABLE mit10k_words
(
  word_id                int       NOT NULL AUTO_INCREMENT,
  word_spell             char(50)  NOT NULL UNIQUE,
  word_source            char(50)  NULL ,
  word_forms             char(255) NULL ,
  word_pron_soundmark    char(50)  NULL ,
  word_pron_sound        char(255) NULL ,
  fre_id                 int       NULL ,
  word_counter           int       DEFAULT 0,
  word_mtime             TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  word_atime             TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (word_id)
) ENGINE=InnoDB;

########################
# Create frequencies table
########################
CREATE TABLE mit10k_frequencies
(
  fre_id             int       NOT NULL AUTO_INCREMENT,
  fre_band           int       NOT NULL UNIQUE,
  fre_description    text      NULL ,
  PRIMARY KEY(fre_id)
) ENGINE=InnoDB;

########################
# Create entries table
########################
CREATE TABLE mit10k_entries
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
CREATE TABLE mit10k_examples
(
  example_id      int       NOT NULL AUTO_INCREMENT,
  example_text    text      NOT NULL ,
  entry_id        int       NOT NULL ,
  PRIMARY KEY(example_id),
) ENGINE=InnoDB;

# FULLTEXT(example_text)

/* }}} Each dict tables */


/* FOREGIN KEY {{{ */
########################
# Create FOREIGN KEY bewteen dicts and dict_types
########################
ALTER TABLE dicts ADD CONSTRAINT fk_dicts_types FOREIGN KEY (dict_type_id)
REFERENCES  dict_types (dict_type_id);

/* Each dict {{{ */
########################
# Create FOREIGN KEY within a specific dict
########################
ALTER TABLE mit10k_words ADD CONSTRAINT mit10k_fk_words_frequencies FOREIGN KEY (fre_id) REFERENCES  mit10k_frequencies (fre_id);
ALTER TABLE mit10k_entries ADD CONSTRAINT mit10k_fk_entries_words FOREIGN KEY (word_id) REFERENCES  mit10k_words (word_id) ON DELETE CASCADE;
ALTER TABLE mit10k_examples ADD CONSTRAINT mit10k_fk_examples_entries FOREIGN KEY (entry_id) REFERENCES  mit10k_entries (entry_id) ON DELETE CASCADE;

########################
# Create index within a specific dict
########################
CREATE UNIQUE INDEX mit10k_index_word ON mit10k_words (word_spell)
/* }}} Each dict */
/* }}} FOREGIN KEY */
