package net.bible.android.control.bookmark;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.bible.android.BibleApplication;
import net.bible.android.activity.R;
import net.bible.android.control.page.CurrentPageManager;
import net.bible.service.db.bookmark.BookmarkDBAdapter;
import net.bible.service.db.bookmark.BookmarkDto;
import net.bible.service.db.bookmark.LabelDto;

import org.crosswire.jsword.passage.Key;

import android.util.Log;
import android.widget.Toast;

public class BookmarkControl implements Bookmark {

	private static final String TAG = "BookmarkControl";
	
	@Override
	public boolean bookmarkCurrentVerse() {
		if (CurrentPageManager.getInstance().isBibleShown()) {
			Key currentVerse = CurrentPageManager.getInstance().getCurrentBible().getSingleKey();
			BookmarkDto bookmarkDto = new BookmarkDto();
			bookmarkDto.setKey(currentVerse);
			
			Toast.makeText(BibleApplication.getApplication().getApplicationContext(), R.string.bookmark_added, Toast.LENGTH_SHORT).show();
			
			return addBookmark(bookmarkDto)!=null;
		}
		return false;
	}

	// pure bookmark methods

	/** get all bookmarks */
	public List<BookmarkDto> getAllBookmarks() {
		BookmarkDBAdapter db = new BookmarkDBAdapter(BibleApplication.getApplication().getApplicationContext());
		db.open();
		List<BookmarkDto> bookmarkList = null;
		try {
			bookmarkList = db.getAllBookmarks();
		} finally {
			db.close();
		}

		return bookmarkList;
	}

	/** create a new bookmark */
	public BookmarkDto addBookmark(BookmarkDto bookmark) {
		BookmarkDBAdapter db = new BookmarkDBAdapter(BibleApplication.getApplication().getApplicationContext());
		db.open();
		BookmarkDto newBookmark = null;
		try {
			newBookmark = db.insertBookmark(bookmark);
		} finally {
			db.close();
		}
		return newBookmark;
	}

	/** delete this bookmark (and any links to labels) */
	public boolean deleteBookmark(BookmarkDto bookmark) {
		boolean bOk = false;
		if (bookmark!=null && bookmark.getId()!=null) {
			BookmarkDBAdapter db = new BookmarkDBAdapter(BibleApplication.getApplication().getApplicationContext());
			db.open();
			bOk = db.removeBookmark(bookmark);
		}		
		return bOk;
	}

	// Label related methods
	/** get bookmarks with the given label */
	public List<BookmarkDto> getBookmarksWithLabel(LabelDto label) {
		BookmarkDBAdapter db = new BookmarkDBAdapter(BibleApplication.getApplication().getApplicationContext());
		db.open();
		List<BookmarkDto> bookmarkList = null;
		try {
			bookmarkList = db.getBookmarksWithLabel(label);
		} finally {
			db.close();
		}

		return bookmarkList;
	}

	/** label the bookmark with these and only these labels */
	public void setBookmarkLabels(BookmarkDto bookmark, List<LabelDto> labels) {
		BookmarkDBAdapter db = new BookmarkDBAdapter(BibleApplication.getApplication().getApplicationContext());
		db.open();
		try {
			List<LabelDto> prevLabels = db.getBookmarkLabels(bookmark);
			
			//find those which have been deleted and remove them
			Set<LabelDto> deleted = new HashSet<LabelDto>(prevLabels);
			deleted.removeAll(labels);
			for (LabelDto label : deleted) {
				Log.d(TAG, "*** removing deleted bookmark label "+label.getName());
				db.removeBookmarkLabelJoin(bookmark, label);
			}
			
			//find those which are new and persist them
			Set<LabelDto> added = new HashSet<LabelDto>(labels);
			added.removeAll(prevLabels);
			for (LabelDto label : added) {
				Log.d(TAG, "*** adding bookmark label "+label.getName());
				db.insertBookmarkLabelJoin(bookmark, label);
			}

		} finally {
			db.close();
		}
	}

	@Override
	public LabelDto addLabel(LabelDto label) {
		BookmarkDBAdapter db = new BookmarkDBAdapter(BibleApplication.getApplication().getApplicationContext());
		db.open();
		LabelDto newLabel = null;
		try {
			newLabel = db.insertLabel(label);
		} finally {
			db.close();
		}
		return newLabel;
	}

	/** delete this bookmark (and any links to labels) */
	public boolean deleteLabel(LabelDto label) {
		boolean bOk = false;
		if (label!=null && label.getId()!=null) {
			BookmarkDBAdapter db = new BookmarkDBAdapter(BibleApplication.getApplication().getApplicationContext());
			db.open();
			bOk = db.removeLabel(label);
		}		
		return bOk;
	}

	@Override
	public List<LabelDto> getAllLabels() {
		BookmarkDBAdapter db = new BookmarkDBAdapter(BibleApplication.getApplication().getApplicationContext());
		db.open();
		List<LabelDto> labelList = null;
		try {
			labelList = db.getAllLabels();
		} finally {
			db.close();
		}

		return labelList;
	}
}