package com.example.android.goodreads.data;

public class Book extends BookListItem {

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

    public Book(BookListItem bookItem) {
        super(bookItem.getBookId(), bookItem.getTitle(), bookItem.getAuthor(), bookItem.getImage());
    }

    public Book(){
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
