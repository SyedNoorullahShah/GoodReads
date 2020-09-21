package com.example.android.goodreads.data;

public class BookListItem {

    private String bookId;
    private String title;
    private String author;
    private String image;

    public BookListItem(String bookId, String bookTitle, String bookAuthor, String bookImage) {
        this.bookId = bookId;
        title = bookTitle;
        author = bookAuthor;
        image = bookImage;
    }

    public BookListItem() {

    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public void setBookId(String bookId) {
        this.bookId = bookId;
    }

    public void setAuthor(String author) {
        this.author = author;
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
}
