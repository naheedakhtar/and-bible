package net.bible.service.download;

import java.util.ArrayList;
import java.util.List;

import org.crosswire.jsword.book.Book;
import org.crosswire.jsword.book.BookMetaData;
import org.crosswire.jsword.book.Books;
import org.crosswire.jsword.book.BooksEvent;
import org.crosswire.jsword.book.BooksListener;

import android.util.Log;

/** some books need renaming after download due to problems with Xiphos module case
 * 
 * @author denha1m
 */
public class XiphosRepo implements BooksListener {

	// see here for info ftp://ftp.xiphos.org/mods.d/
	private static final String XIPHOS_REPOSITORY = "Xiphos";
	
	private static List<XiphosRepoBook> xiphosRepoBookList = new ArrayList<XiphosRepoBook>();
	static {
		xiphosRepoBookList.add(new XiphosRepoBook("gill", "Gill", "DataPath=./modules/comments/zcom/gill/\nModDrv=zCom\nSourceType=ThML\nBlockType=BOOK\nCompressType=ZIP\nLang=en\nDescription=John Gill's Expositor\nAbout="));
// This shows the greek word in addition to the content of the default Strongs dictionary but some of the Greek characters don't display correctly.  
//		xiphosRepoBookList.add(new XiphosRepoBook("strongsrealgreek", "StrongsRealGreek", "DataPath=./modules/lexdict/rawld4/strongsrealgreek/strongsrealgreek\nModDrv=RawLD4\nLang=en\nFeature=GreekDef\nVersion=1.4-100511\nEncoding=UTF-8\nSourceType=ThML\nDescription=Strongs Real Greek Bible Dictionary\nAbout=Text pulled from Ulrik Petersen's content at http://morphgnt.org/projects/strongs-dictionary. In 1996, Michael Grier produced an e-text of Strong's dictionary. He entered every single letter, and did some proof-reading, but transliterated the Greek. In 2006, Ulrik Petersen took Michael Grier's e-text in the version published by the SWORD project and added Greek in UTF-8 where applicable, while transforming the text to XML."));
		xiphosRepoBookList.add(new XiphosRepoBook("augustine", "Augustine", "DataPath=./modules/genbook/rawgenbook/augustine/augustine\nModDrv=RawGenBook\nLang=en\nEncoding=UTF-8\nSourceType=ThML\nDescription=St. Augustine: Works\nAbout="));
		xiphosRepoBookList.add(new XiphosRepoBook("finneysystheo", "FinneySysTheo", "DataPath=./modules/genbook/rawgenbook/finneysystheo/finneysystheo\nModDrv=RawGenBook\nLang=en\nEncoding=UTF-8\nSourceType=ThML\nDescription=Finney's Systematic Theology\nAbout="));
		xiphosRepoBookList.add(new XiphosRepoBook("hodgesystheo", "HodgeSysTheo", "DataPath=./modules/genbook/rawgenbook/hodgesystheo/systheo\nModDrv=RawGenBook\nSourceType=ThML\nGlobalOptionFilter=ThMLFootnotes\nEncoding=UTF-8\nLang=en\nDescription=Hodge's Systematic Theology - Volumes I/II/III/IV\nAbout="));
		xiphosRepoBookList.add(new XiphosRepoBook("lifetimes", "LifeTimes", "DataPath=./modules/genbook/rawgenbook/lifetimes/lifetimes\nEncoding=UTF-8\nModDrv=RawGenBook\nSourceType=ThML\nLang=en\nDescription=The Life and Times of Jesus the Messiah\nAbout="));
		xiphosRepoBookList.add(new XiphosRepoBook("traintwelve", "TrainTwelve", "DataPath=./modules/genbook/rawgenbook/traintwelve/traintwelve\nModDrv=RawGenBook\nLang=en\nEncoding=UTF-8\nSourceType=ThML\nDescription=The Training of the Twelve\nAbout="));
		xiphosRepoBookList.add(new XiphosRepoBook("polbibtysia", "PolBibTysia", "DataPath=./modules/texts/rawtext/polbibtysia/\nModDrv=RawText\nSourceType=ThML\nLang=pl\nEncoding=UTF-8\nVersion=1.080330\nDescription=Biblia Tysiaclecia\nAbout="));
		xiphosRepoBookList.add(new XiphosRepoBook("chipinyin", "ChiPinyin", "DataPath=./modules/texts/rawtext/chipinyin/\nModDrv=RawText\nEncoding=UTF-8\nLang=zh\nSourceType=ThML\nVersion=1.080413\nDescription=Pinyin (Romanized Chinese)\nAbout=This Romanized Chinese 'Pinyin' Bible is publicly available from the Word Project, http://www.wordproject.org/. \\par\\par Errata: Mark 3 is missing."));
// causes error after download - unzipping?		xiphosRepoBookList.add(new XiphosRepoBook("shaw", "Shaw", "DataPath=./modules/genbook/rawgenbook/shaw/shaw\nModDrv=RawGenBook\nLang=en\nEncoding=UTF-8\nSourceType=OSIS\nDescription=Robert Shaw, The Reformed Faith\nAbout=Robert Shaw, Scottish Presbyterian (1795-1863) was a leader in the Original Secession Church and a minister at Whitburn. His commentary 'The Reformed Faith, An Exposition of the Westminster Confession of Faith', is the standard commentary and companion to the Westminster Confession of Faith. The source for this text was obtained from www.reformed.org. With thanks to Ed Walsh."));
// causes error after download - unzipping?		xiphosRepoBookList.add(new XiphosRepoBook("kretzmann", "Kretzmann", "DataPath=./modules/comments/zcom/kretzmann/\nModDrv=zCom\nBlockType=CHAPTER\nSourceType=OSIS\nCompressType=ZIP\nEncoding=UTF-8\nLang=en\nLCSH=Bible--Commentaries.\nVersion=1.20110405\nDescription=Kretzmann Popular Commentary\nAbout=Popular Commentary of the Bible by\\par\\par PAUL E. KRETZMANN, M.A., PhD., B.D."));
	}
	
	private static int booksToListenForCount = 0;
	
	/** get a list of books that are available in Xiphos repo and seem to work in And Bible
	 */
	public List<Book> getXiphosRepoBooks() {
		List<Book> bookList = new ArrayList<Book>();
		for (XiphosRepoBook xiphosRepoBook : xiphosRepoBookList) {
			try {
		        String conf = "[" + xiphosRepoBook.downloadFile + "]\n" + xiphosRepoBook.otherProperties;
		        Book repoBook = FakeSwordBookFactory.createFakeRepoBook(xiphosRepoBook.normalInitials, conf, XIPHOS_REPOSITORY);
		        bookList.add(repoBook);
			} catch (Exception e) {
				Log.e(TAG, e.getMessage());
			}
		}
		
		return bookList;		
	}
	
	private static final String TAG = "PostDownloadAction";

	/** true if book is in Xiphos repo
	 */
	public boolean needsPostDownloadAction(Book book) {
		return findRepoBook(book)!=null;
	}

	/** add a listener to handle module rename after download
	 */
	public void addHandler(Book book) {
		// If you want to know about new books as they arrive:
		if (needsPostDownloadAction(book)) {
			Log.d(TAG, "Adding BooksListener for "+book);
	        Books.installed().addBooksListener(this);
	        booksToListenForCount++;
		}
	}

	/** called after download of book from Xiphos repo completes to rename Module name to be camel case
	 */
	@Override
	public void bookAdded(BooksEvent ev) {
		Book book = ev.getBook();
		XiphosRepoBook xiphosRepoBook = findRepoBook(book);
		if (xiphosRepoBook!=null) {
			try {
		        String conf = "[" + xiphosRepoBook.normalInitials + "]\n" + xiphosRepoBook.otherProperties;
		        BookMetaData bmd = FakeSwordBookFactory.createRepoSBMD(xiphosRepoBook.normalInitials, conf);
		        book.setBookMetaData(bmd);
				Log.d(TAG, "Check initials "+book.getInitials());
			} catch (Exception e) {
				Log.e(TAG, e.getMessage());
			}
			
			booksToListenForCount--;
			if (booksToListenForCount==0) {
		        Books.installed().removeBooksListener(this);
			}
		}
	}
	
	@Override
	public void bookRemoved(BooksEvent ev) {
		//ignore
	}

	/** find book in Xiphos repo book list or return null
	 */
	private XiphosRepoBook findRepoBook(Book book) {
		XiphosRepoBook foundBook = null;
		for (XiphosRepoBook repoBook : xiphosRepoBookList) {
			if (repoBook.normalInitials.equalsIgnoreCase(book.getInitials())) {
				foundBook = repoBook;
			}
		}
		return foundBook;
	}
	
	private static class XiphosRepoBook {
		String downloadFile;
		String normalInitials;
		String otherProperties;
		
		private XiphosRepoBook(String downloadFile, String normalInitials, String otherProperties) {
			this.downloadFile = downloadFile;
			this.normalInitials = normalInitials;
			this.otherProperties = otherProperties;
		}
	}
}
