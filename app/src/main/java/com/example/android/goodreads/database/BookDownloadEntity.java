package com.example.android.goodreads.database;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import com.example.android.goodreads.data.Book;

@Entity(tableName = "download_table")
public class BookDownloadEntity {

    @PrimaryKey(autoGenerate = true)
    private int id;

    private String bookId;
    private String title;
    private String author;
    private String image;
    private String publisher;
    private String publishedDate;
    private String description;
    private int pageCount;
    private String category;
    private double rating;
    private int no_ratings;
    private String lang;
    private String webReaderLink;
    private String downloadLink;

    public BookDownloadEntity(){

    }

    @Ignore
    public BookDownloadEntity(Book book) {
        bookId = book.getBookId();
        title = book.getTitle();
        author = book.getAuthor();
        image = book.getImage();
        publisher = book.getPublisher();
        publishedDate = book.getPublishedDate();
        description = book.getDescription();
        pageCount = book.getPageCount();
        category = book.getCategory();
        rating = book.getRating();
        no_ratings = book.getNo_ratings();
        lang = book.getLang();
        webReaderLink = book.getWebReaderLink();
        downloadLink = book.getDownloadLink();
    }

    public void setId(int id) {
        this.id = id;
    }


    public void setBookId(String bookId) {
        this.bookId = bookId;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }

    public void setPublishedDate(String publishedDate) {
        this.publishedDate = publishedDate;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setPageCount(int pageCount) {
        this.pageCount = pageCount;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public void setRating(double rating) {
        this.rating = rating;
    }

    public void setNo_ratings(int no_ratings) {
        this.no_ratings = no_ratings;
    }

    public void setLang(String lang) {
        this.lang = lang;
    }

    public void setWebReaderLink(String webReaderLink) {
        this.webReaderLink = webReaderLink;
    }

    public void setDownloadLink(String downloadLink) {
        this.downloadLink = downloadLink;
    }

    public int getId() {
        return id;
    }

    public String getBookId() {
        return bookId;
    }

    public String getTitle() {
        return title;
    }

    public String getAuthor() {
        return author;
    }

    public String getImage() {
        return image;
    }

    public String getPublisher() {
        return publisher;
    }

    public String getPublishedDate() {
        return publishedDate;
    }

    public String getDescription() {
        return description;
    }

    public int getPageCount() {
        return pageCount;
    }

    public String getCategory() {
        return category;
    }

    public double getRating() {
        return rating;
    }

    public int getNo_ratings() {
        return no_ratings;
    }

    public String getLang() {
        return lang;
    }

    public String getWebReaderLink() {
        return webReaderLink;
    }

    public String getDownloadLink() {
        return downloadLink;
    }


}
