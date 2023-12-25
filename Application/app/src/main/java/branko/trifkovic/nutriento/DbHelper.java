package branko.trifkovic.nutriento;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

public class DbHelper extends SQLiteOpenHelper {

    private final String TABLE_MACROS = "MACRONUTRIENTS";
    private final String COLUMN_ID = "id";
    private final String COLUMN_DESCRIPTION = "description";
    private final String COLUMN_CALORIES = "calories";
    private final String COLUMN_PROTEINS = "proteins";
    private final String COLUMN_CARBS = "carbs";
    private final String COLUMN_FATS = "fats";
    private final String CREATE_TABLE_MACROS = "CREATE TABLE IF NOT EXISTS " + TABLE_MACROS + " (" + COLUMN_ID + " INTEGER, "
            + COLUMN_DESCRIPTION + " TEXT, " + COLUMN_CALORIES + " REAL, " + COLUMN_PROTEINS + " REAL, " + COLUMN_CARBS
            + " REAL, " + COLUMN_FATS + " REAL) ;";

    public DbHelper(@Nullable Context context, @Nullable String name, @Nullable SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(CREATE_TABLE_MACROS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }

    public ModelClass readClass(int id) {
        SQLiteDatabase db = getReadableDatabase();
        boolean rv;

        Cursor cursor = db.query(TABLE_MACROS, null, COLUMN_ID + " =?", new String[] {Integer.toString(id)}, null, null, null);

        cursor.moveToFirst();

        ModelClass mc = createClass(cursor);

        close();

        return mc;
    }

    private ModelClass createClass(Cursor cursor) {
        int id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID));
        String description = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DESCRIPTION));
        double calories = cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_CALORIES));
        double proteins = cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_PROTEINS));
        double carbs = cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_CARBS));
        double fats = cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_FATS));

        return new ModelClass(id, description, calories, proteins, carbs, fats);
    }
}
