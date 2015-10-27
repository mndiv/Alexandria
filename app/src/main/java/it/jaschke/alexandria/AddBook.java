package it.jaschke.alexandria;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.util.ArrayList;

import it.jaschke.alexandria.api.Callback;
import it.jaschke.alexandria.services.BookService;


public class AddBook extends Fragment {
    private boolean mHasNetwork;
    private static final String TAG = "INTENT_TO_SCAN_ACTIVITY";
    private EditText ean;
    private Button searchView;
    private final int LOADER_ID = 1;
    private View rootView;
    private final String EAN_CONTENT = "eanContent";


    public AddBook() {
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (ean != null) {
            outState.putString(EAN_CONTENT, ean.getText().toString());
        }
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    /**
     * Get results of scanning and pass to EditText
     */
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            if (result.getContents() != null) {
                isNetworkAvailable();
                if (mHasNetwork) {
                    //Get the ean number
                    String isbnNumber = result.getContents();
                    ean.setText(isbnNumber);

                }
            }
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        rootView = inflater.inflate(R.layout.fragment_add_book, container, false);
        ean = (EditText) rootView.findViewById(R.id.ean);

        searchView = (Button)rootView.findViewById(R.id.search_button);
        searchView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!validateIsbn()) return;

                String isbnNumber = ean.getText().toString();
                ean.setText("");
                ean.setHint(R.string.input_hint);

                isNetworkAvailable();
                if (mHasNetwork) {
                    Intent bookIntent = new Intent(getActivity(), BookService.class);
                    bookIntent.putExtra(BookService.EAN, isbnNumber);
                    bookIntent.setAction(BookService.FETCH_BOOK);
                    getActivity().startService(bookIntent);

                    ((Callback) getActivity()).onItemSelected(isbnNumber);
                } else {
                    Toast.makeText(getActivity(), R.string.empty_book_list_no_network,
                            Toast.LENGTH_SHORT).show();
                }
            }
        });

        ean.addTextChangedListener(new MyTextWatcher(ean));

/*
        ean.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                //no need
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                //no need
            }

            @Override
            public void afterTextChanged(Editable s) {
                String ean = s.toString();
                //catch isbn10 numbers
                if (ean.length() == 10 && !ean.startsWith("978")) {
                    ean = "978" + ean;
                }
                if (ean.length() < 13) {
                    //clearFields();
                    return;
                }
                isNetworkAvailable();
                if (mHasNetwork) {
                    //Once we have an ISBN, start a book intent
                    Intent bookIntent = new Intent(getActivity(), BookService.class);
                    bookIntent.putExtra(BookService.EAN, ean);
                    bookIntent.setAction(BookService.FETCH_BOOK);
                    getActivity().startService(bookIntent);

                    ((Callback) getActivity()).onItemSelected(ean);
                   // AddBook.this.restartLoader();
                    Log.v("AddBook", "After Bookdetail");

                } else {
                    Toast.makeText(getActivity(), R.string.empty_book_list_no_network,
                            Toast.LENGTH_SHORT).show();
                }
            }
        });*/


        rootView.findViewById(R.id.scan_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // This is the callback method that the system will invoke when your button is
                // clicked. You might do this by launching another app or by including the
                //functionality directly in this app.
                // Hint: Use a Try/Catch block to handle the Intent dispatch gracefully, if you
                // are using an external app.
                //when you're done, remove the toast below.
                CharSequence text = "This button should let you scan a book for its barcode!";
                int duration = Toast.LENGTH_SHORT;
                ArrayList<String> formats = new ArrayList<>();
                formats.add("EAN_13");
                IntentIntegrator.forSupportFragment(AddBook.this).setPrompt("Scan a barcode")
                        .setCameraId(0)
                        .setBeepEnabled(false)
                        .initiateScan();

            }
        });

//        rootView.findViewById(R.id.save_button).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                ean.setText("");
//            }
//        });

//        rootView.findViewById(R.id.delete_button).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Intent bookIntent = new Intent(getActivity(), BookService.class);
//                bookIntent.putExtra(BookService.EAN, ean.getText().toString());
//                bookIntent.setAction(BookService.DELETE_BOOK);
//                getActivity().startService(bookIntent);
//                ean.setText("");
//            }
//        });

        if (savedInstanceState != null) {
            ean.setText(savedInstanceState.getString(EAN_CONTENT));
            ean.setHint(R.string.input_hint);
        }

        return rootView;
    }

    /**
     * TextWatcher class to watch EditText
     */
    private class MyTextWatcher implements TextWatcher {

        private final View view;

        private MyTextWatcher(View view) {
            this.view = view;
        }

        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        }

        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        }

        public void afterTextChanged(Editable editable) {
            if (view.getId() == R.id.ean)
                validateIsbn();
        }
    }

    /**
     * Helper method to validate ISBN
     */
    private boolean validateIsbn() {
        String isbnNumber = ean.getText().toString();
        if (isbnNumber.length() == 10 && !isbnNumber.startsWith("978")) {
            isbnNumber = "978" + isbnNumber;
        }
        if(isbnNumber.length()<13){

            //Toast.makeText(getActivity(),R.string.error_invalid_isbn_number,Toast.LENGTH_SHORT).show();
            return false;
        } else {

            return true;
        }
    }

    /*
    private void restartLoader() {
        getLoaderManager().restartLoader(LOADER_ID, null, this);
    }

    @Override
    public android.support.v4.content.Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if (ean.getText().length() == 0) {
            return null;
        }
        String eanStr = ean.getText().toString();
        if (eanStr.length() == 10 && !eanStr.startsWith("978")) {
            eanStr = "978" + eanStr;
        }
        return new CursorLoader(
                getActivity(),
                AlexandriaContract.BookEntry.buildFullBookUri(Long.parseLong(eanStr)),
                null,
                null,
                null,
                null
        );
    }

    @Override
    public void onLoadFinished(android.support.v4.content.Loader<Cursor> loader, Cursor data) {
        if (!data.moveToFirst()) {
            return;
        }

        String bookTitle = data.getString(data.getColumnIndex(AlexandriaContract.BookEntry.TITLE));
        ((TextView) rootView.findViewById(R.id.bookTitle)).setText(bookTitle);

        String bookSubTitle = data.getString(data.getColumnIndex(AlexandriaContract.BookEntry.SUBTITLE));
        ((TextView) rootView.findViewById(R.id.bookSubTitle)).setText(bookSubTitle);

        rootView.findViewById(R.id.by).setVisibility(View.VISIBLE);
        String authors = data.getString(data.getColumnIndex(AlexandriaContract.AuthorEntry.AUTHOR));
        if (authors != null) {
            String[] authorsArr = authors.split(",");
            ((TextView) rootView.findViewById(R.id.authors)).setLines(authorsArr.length);
            ((TextView) rootView.findViewById(R.id.authors)).setText(authors.replace(",", "\n"));

        }
        String imgUrl = data.getString(data.getColumnIndex(AlexandriaContract.BookEntry.IMAGE_URL));
        if (Patterns.WEB_URL.matcher(imgUrl).matches()) {
            new DownloadImage((ImageView) rootView.findViewById(R.id.bookCover)).execute(imgUrl);
            rootView.findViewById(R.id.bookCover).setVisibility(View.VISIBLE);
        }

        String categories = data.getString(data.getColumnIndex(AlexandriaContract.CategoryEntry.CATEGORY));
        ((TextView) rootView.findViewById(R.id.categories)).setText(categories);

        rootView.findViewById(R.id.save_button).setVisibility(View.VISIBLE);
        rootView.findViewById(R.id.delete_button).setVisibility(View.VISIBLE);
    }

    @Override
    public void onLoaderReset(android.support.v4.content.Loader<Cursor> loader) {

    }
    */

    /**
     * Returns true if the network is available or about to become available.
     */
    public void isNetworkAvailable() {
        ConnectivityManager cm =
                (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        mHasNetwork = activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();
    }

//    private void clearFields() {
//        ((TextView) rootView.findViewById(R.id.bookTitle)).setText("");
//        ((TextView) rootView.findViewById(R.id.bookSubTitle)).setText("");
//        ((TextView) rootView.findViewById(R.id.authors)).setText("");
//        ((TextView) rootView.findViewById(R.id.categories)).setText("");
//        rootView.findViewById(R.id.bookCover).setVisibility(View.INVISIBLE);
//        rootView.findViewById(R.id.save_button).setVisibility(View.INVISIBLE);
//        rootView.findViewById(R.id.delete_button).setVisibility(View.INVISIBLE);
//    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        activity.setTitle(R.string.scan);
    }
}
