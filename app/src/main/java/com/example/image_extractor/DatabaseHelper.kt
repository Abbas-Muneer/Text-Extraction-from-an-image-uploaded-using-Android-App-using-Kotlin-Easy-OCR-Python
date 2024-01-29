import android.content.ContentValues
import android.content.Context
import android.database.SQLException
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteDatabase.openDatabase
import android.database.sqlite.SQLiteException
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class DatabaseHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "image_database.db"
        private const val DATABASE_VERSION = 1
        const val DATABASE_PATH = "/data/data/com.example.image_extractor/databases/"
        private const val TABLE_NAME = "image_entries"
        private const val COLUMN_ID = "_id"
        private const val COLUMN_COMPETITOR_NAME = "competitor_name"
        private const val COLUMN_DATE = "date"
        private const val COLUMN_IMAGE = "image_path"

        private var mDataBase: SQLiteDatabase? = null
        private val TAG: String = "Database Helper"
    }

    private val DB_FILE: File = context.getDatabasePath(DATABASE_NAME)
    private val mContext: Context = context


    override fun onCreate(db: SQLiteDatabase?) {

        val createTableQuery = """
            CREATE TABLE $TABLE_NAME (
                $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_COMPETITOR_NAME TEXT,
                $COLUMN_DATE TEXT,
                $COLUMN_IMAGE BLOB  -- Store image as BLOB (Binary Large Object)
            )
        """.trimIndent()

        db?.execSQL(createTableQuery)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        // Handle upgrades if needed
    }

    fun insertData(competitorName: String, date: String, imageBytes: ByteArray) {

        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_COMPETITOR_NAME, competitorName)
            put(COLUMN_DATE, date)
            put(COLUMN_IMAGE, imageBytes)
        }
        Log.e(TAG, "Successfully inserted data into AuditLog Table")

        db.insert(TABLE_NAME, null, values)
        //db.close()
    }
}
