package com.example.bgangu.inventoryappbgangu;

import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.bgangu.inventoryappbgangu.data.InventoryContract;
import com.example.bgangu.inventoryappbgangu.data.InventoryContract.InventoryEntry;

public class EditorActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int EXISTING_ITEM_LOADER = 0;
    private EditText mNameEditText;
    private EditText mPriceEditText;
    private EditText mQuantityEditText;
    private EditText mSupplierEditText;
    private EditText mPhoneNumberEditText;
    private Button mOrderButton;
    private Button mQuantityIncreaseButton;
    private Button mQuantityDecreaseButton;
    private Uri mCurrentItemUri;
    private boolean hasItemChanged = false;
    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            hasItemChanged = true;
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        Intent intent = getIntent();
        mCurrentItemUri = intent.getData();
        if (mCurrentItemUri == null) {
            setTitle(R.string.add_a_product);
            mOrderButton = findViewById(R.id.product_order_button);
            mOrderButton.setVisibility(View.GONE);
            mQuantityEditText = findViewById(R.id.edit_product_quantity);
            mQuantityEditText.setText("" + 0);
        } else {
            setTitle(R.string.edit_product);
            getLoaderManager().initLoader(EXISTING_ITEM_LOADER, null, this);
        }

        mNameEditText = findViewById(R.id.edit_product_name);
        mPriceEditText = findViewById(R.id.edit_product_price);
        mQuantityEditText = findViewById(R.id.edit_product_quantity);
        mSupplierEditText = findViewById(R.id.edit_product_supplier);
        mPhoneNumberEditText = findViewById(R.id.edit_product_phone_number);

        mNameEditText.setOnTouchListener(mTouchListener);
        mPriceEditText.setOnTouchListener(mTouchListener);
        mQuantityEditText.setOnTouchListener(mTouchListener);
        mSupplierEditText.setOnTouchListener(mTouchListener);
        mPhoneNumberEditText.setOnTouchListener(mTouchListener);

        mQuantityIncreaseButton = findViewById(R.id.increase_quantity_button);
        mQuantityIncreaseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                increaseQuantity();
            }
        });

        mQuantityDecreaseButton = findViewById(R.id.decrease_quantity_button);
        mQuantityDecreaseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                decreaseQuantity();
            }
        });

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        if (mCurrentItemUri == null) {
            MenuItem menuItem = menu.findItem(R.id.action_delete);
            menuItem.setVisible(false);
        }
        return true;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String[] projection = {
                InventoryEntry._ID,
                InventoryEntry.COLUMN_PRODUCT_NAME,
                InventoryEntry.COLUMN_PRODUCT_PRICE,
                InventoryEntry.COLUMN_PRODUCT_QUANTITY,
                InventoryEntry.COLUMN_PRODUCT_SUPPLIER_NAME,
                InventoryEntry.COLUMN_PRODUCT_SUPPLIER_PHONE_NUMBER};

        return new CursorLoader(this,
                mCurrentItemUri,
                projection,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (cursor == null || cursor.getCount() < 1) {
            return;
        }

        if (cursor.moveToFirst()) {
            int nameColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_PRODUCT_NAME);
            int priceColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_PRODUCT_PRICE);
            int quantityColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_PRODUCT_QUANTITY);
            int supplierColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_PRODUCT_SUPPLIER_NAME);
            int phoneNumberColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_PRODUCT_SUPPLIER_PHONE_NUMBER);

            String name = cursor.getString(nameColumnIndex);
            int price = cursor.getInt(priceColumnIndex);
            int quantity = cursor.getInt(quantityColumnIndex);
            String supplier = cursor.getString(supplierColumnIndex);
            final String phoneNumber = cursor.getString(phoneNumberColumnIndex);

            mNameEditText.setText(name);
            mPriceEditText.setText(Integer.toString(price));
            mQuantityEditText.setText(Integer.toString(quantity));
            mSupplierEditText.setText(supplier);
            mPhoneNumberEditText.setText(phoneNumber);

            mOrderButton = findViewById(R.id.product_order_button);
            mOrderButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(Intent.ACTION_DIAL);
                    intent.setData(Uri.parse("tel:" + phoneNumber));
                    startActivity(intent);
                }
            });
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mNameEditText.setText("");
        mPriceEditText.setText("");
        mQuantityEditText.setText("");
        mSupplierEditText.setText("");
        mPhoneNumberEditText.setText("");
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_save:
                saveItem();
                return true;
            case R.id.action_delete:
                showDeleteConfirmationDialog();
                return true;
            case android.R.id.home:
                if (!hasItemChanged) {
                    NavUtils.navigateUpFromSameTask(EditorActivity.this);
                    return true;
                }
                DialogInterface.OnClickListener discardButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                NavUtils.navigateUpFromSameTask(EditorActivity.this);
                            }
                        };
                showUnsavedChangesDialog(discardButtonClickListener);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void showUnsavedChangesDialog(
            DialogInterface.OnClickListener discardButtonClickListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_dialog_msg);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void saveItem() {
        String name = mNameEditText.getText().toString().trim();
        String price = mPriceEditText.getText().toString().trim();
        String quantity = mQuantityEditText.getText().toString().trim();
        String supplier = mSupplierEditText.getText().toString().trim();
        String phoneNumber = mPhoneNumberEditText.getText().toString().trim();

        ContentValues values = new ContentValues();

        if (TextUtils.isEmpty(name)) {
            Toast.makeText(this, "Name filed can't be blank", Toast.LENGTH_SHORT).show();
            return;
        } else {
            values.put(InventoryEntry.COLUMN_PRODUCT_NAME, name);
        }

        int productPrice = 0;
        if (!TextUtils.isEmpty(price)) {
            try {
                productPrice = Integer.parseInt(price);
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Price should be valid", Toast.LENGTH_SHORT).show();
            }
        }

        if (productPrice <= 0) {
            Toast.makeText(this, "Price should be valid", Toast.LENGTH_SHORT).show();
            return;
        } else {
            values.put(InventoryEntry.COLUMN_PRODUCT_PRICE, productPrice);
        }


        int productQuantity = 0;
        if (!TextUtils.isEmpty(quantity)) {
            try {
                productQuantity = Integer.parseInt(quantity);
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Enter valid Quantity", Toast.LENGTH_SHORT).show();
            }

        }

        if (productQuantity < 0) {
            Toast.makeText(this, "Quantity should not be less than 0", Toast.LENGTH_SHORT).show();
            return;
        } else {
            values.put(InventoryEntry.COLUMN_PRODUCT_QUANTITY, productQuantity);
        }

        if (TextUtils.isEmpty(supplier)) {
            Toast.makeText(this, "Supplier field can't be left blank", Toast.LENGTH_SHORT).show();
            return;
        } else {
            values.put(InventoryEntry.COLUMN_PRODUCT_SUPPLIER_NAME, supplier);
        }

        if (TextUtils.isEmpty(phoneNumber)) {
            Toast.makeText(this, "Phone Number is either invalid or null", Toast.LENGTH_SHORT).show();
            return;
        } else {
            try {
                long number = Long.parseLong(phoneNumber);
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Phone Number is invalid", Toast.LENGTH_SHORT).show();
                return;
            }
            values.put(InventoryEntry.COLUMN_PRODUCT_SUPPLIER_PHONE_NUMBER, phoneNumber);
        }

        if (mCurrentItemUri == null) {

            Uri newUri = getContentResolver().insert(InventoryEntry.CONTENT_URI, values);

            if (newUri == null) {
                Toast.makeText(this, getString(R.string.editor_insert_item_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, getString(R.string.editor_insert_item_successful),
                        Toast.LENGTH_SHORT).show();
            }
            finish();
        } else {
            int rowsAffected = getContentResolver().update(mCurrentItemUri, values, null, null);

            if (rowsAffected == 0) {
                Toast.makeText(this, getString(R.string.editor_update_item_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, getString(R.string.editor_update_item_successful),
                        Toast.LENGTH_SHORT).show();
            }
            finish();
        }
    }

    private void showDeleteConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                deleteItem();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void deleteItem() {
        if (mCurrentItemUri != null) {
            int rowsDeleted = getContentResolver().delete(mCurrentItemUri, null, null);

            if (rowsDeleted == 0) {
                Toast.makeText(this, getString(R.string.editor_delete_item_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, getString(R.string.editor_delete_item_successful),
                        Toast.LENGTH_SHORT).show();
            }
        }

        finish();
    }

    private void increaseQuantity() {
        int currentQuantity;
        if (mQuantityEditText.getText().toString().trim() == null) {
            mQuantityEditText.setText("" + 0);
        } else {
            currentQuantity = Integer.parseInt(mQuantityEditText.getText().toString().trim());
            int quantity = currentQuantity + 1;
            mQuantityEditText.setText("" + quantity);
        }
    }

    private void decreaseQuantity() {
        int currentQuantity;

        if (mQuantityEditText.getText().toString().trim() == null) {
            mQuantityEditText.setText("" + 0);
        } else {
            currentQuantity = Integer.parseInt(mQuantityEditText.getText().toString().trim());
            if (currentQuantity == 0) {
                Toast.makeText(this, getString(R.string.less_than_zero), Toast.LENGTH_SHORT).show();
            } else {
                int quantity = currentQuantity - 1;
                mQuantityEditText.setText("" + quantity);
            }
        }
    }
}
