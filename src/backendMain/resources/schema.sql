-- based on https://github.com/gothinkster/scala-play-realworld-example-app/blob/master/conf/evolutions/default/1.sql

CREATE TABLE IF NOT EXISTS users (
  id serial NOT NULL PRIMARY KEY,
  username VARCHAR(255) NOT NULL,
  email VARCHAR(255) NOT NULL,
  password VARCHAR(255) NOT NULL,
  bio VARCHAR(1024),
  image VARCHAR(255),
  CONSTRAINT users_email_unique UNIQUE (email),
  CONSTRAINT users_username_unique UNIQUE (username)
);

CREATE TABLE IF NOT EXISTS articles (
  id serial NOT NULL PRIMARY KEY,
  slug VARCHAR(255) NOT NULL,
  title VARCHAR(300) NOT NULL,
  description VARCHAR(255) NOT NULL,
  body VARCHAR NOT NULL,
  created_at TIMESTAMP WITH TIME ZONE NOT NULL,
  updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
  author_id INTEGER NOT NULL,
  FOREIGN KEY (author_id) REFERENCES users(id),
  CONSTRAINT articles_slug_unique UNIQUE(slug)
);

CREATE TABLE IF NOT EXISTS tags (
  id serial NOT NULL PRIMARY KEY,
  name VARCHAR(255) NOT NULL,
  CONSTRAINT tag_name_unique UNIQUE(name)
);

CREATE TABLE IF NOT EXISTS articles_tags (
  id serial NOT NULL PRIMARY KEY,
  article_id INTEGER NOT NULL,
  tag_id INTEGER NOT NULL,
  FOREIGN KEY (article_id) REFERENCES articles(id),
  FOREIGN KEY (tag_id) REFERENCES tags(id),
  CONSTRAINT article_tag_id_unique UNIQUE (article_id, tag_id)
);

CREATE TABLE IF NOT EXISTS comments (
  id serial NOT NULL PRIMARY KEY,
  body VARCHAR(4096) NOT NULL,
  article_id INTEGER NOT NULL,
  author_id INTEGER NOT NULL,
  created_at TIMESTAMP WITH TIME ZONE NOT NULL,
  updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
  FOREIGN KEY (article_id) REFERENCES articles(id),
  FOREIGN KEY (author_id) REFERENCES users(id)
);

CREATE TABLE IF NOT EXISTS follow_associations (
  id serial NOT NULL PRIMARY KEY,
  follower_id INTEGER NOT NULL,
  followed_id INTEGER NOT NULL,
  FOREIGN KEY (follower_id) REFERENCES users(id),
  FOREIGN KEY (followed_id) REFERENCES users(id),
  CONSTRAINT follow_associations_follower_followed_unq UNIQUE (follower_id, followed_id)
);

CREATE TABLE IF NOT EXISTS favorite_associations (
  id serial NOT NULL PRIMARY KEY,
  user_id INTEGER NOT NULL,
  favorited_id INTEGER NOT NULL,
  FOREIGN KEY (user_id) REFERENCES users(id),
  FOREIGN KEY (favorited_id) REFERENCES articles(id),
  CONSTRAINT favorite_associations_user_favorited_unq UNIQUE (user_id, favorited_id)
);
