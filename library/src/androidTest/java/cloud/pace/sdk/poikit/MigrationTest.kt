package cloud.pace.sdk.poikit

import androidx.room.Room
import androidx.room.testing.MigrationTestHelper
import androidx.test.platform.app.InstrumentationRegistry
import cloud.pace.sdk.poikit.database.POIKitDatabase
import junit.framework.TestCase.assertEquals
import org.junit.Rule
import org.junit.Test
import java.io.IOException

class MigrationTest {
    private val TEST_DB = "migration-test"

    @get:Rule
    val helper: MigrationTestHelper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        POIKitDatabase::class.java
    )

    @Test
    @Throws(IOException::class)
    fun migrateAll() {
        // Create earliest version of the database.
        helper.createDatabase(TEST_DB, 1).apply {
            // Seed with some data
            execSQL(
                "INSERT INTO \"GasStation\" VALUES ('Shell','[{\"days\":[\"MONDAY\",\"TUESDAY\",\"WEDNESDAY\",\"THURSDAY\",\"FRIDAY\",\"SATURDAY\",\"SUNDAY\"]," +
                    "\"hours\":[{\"from\":\"0\",\"to\":\"0\"}],\"rule\":\"OPEN\"}]','[{\"name\":\"Diesel FuelSave\",\"price\":1.249,\"type\":\"DIESEL\"},{\"name\":\"V-Power Diesel\"," +
                    "\"price\":1.439,\"type\":\"DIESEL_PREMIUM\"},{\"name\":\"V-Power Racing\",\"price\":1.609,\"type\":\"RON100\"},{\"name\":\"Super FuelSave E10\",\"price\":1.399," +
                    "\"type\":\"E10_RON98\"},{\"name\":\"Super FuelSave 95\",\"price\":1.419,\"type\":\"E5\"}]','EUR',1573545854000,'[\"MASTER_CARD\",\"VISA\",\"CASH\",\"AMERICAN_EXPRESS\"," +
                    "\"DINERS_CLUB\",\"EUROSHELL\",\"PAYPAL\"]',0,'[\"TOILET\",\"SHOP\",\"ATM\"]','[\"BAKERY\",\"CAFE\"]','[\"SHELL_CLUBSMART\"]','[]','[\"CAR_WASH\",\"TYRE_AIR\",\"VACUUM\"," +
                    "\"SCREEN_WASH_WATER\"]','[\"AD_BLUE\"]',0,1573547242518,'496a4123-4acc-4f03-9488-bc85a76c41aa','[{\"commandType\":\"MOVETO\",\"locationPoint\":{\"lat\":48.935194152219424," +
                    "\"lon\":8.42889279127121}}]','DE','Ettlingen','76275',NULL,NULL,'Pforzheimer Str.','110-116','c=DE;l=Ettlingen;pc=76275;s=Pforzheimer Str.;hn=110-116');"
            )
            execSQL(
                "INSERT INTO \"GasStation\" VALUES ('Shell','[{\"days\":[\"MONDAY\",\"TUESDAY\",\"WEDNESDAY\",\"THURSDAY\",\"FRIDAY\",\"SATURDAY\",\"SUNDAY\"]," +
                    "\"hours\":[{\"from\":\"0\",\"to\":\"0\"}],\"rule\":\"OPEN\"}]','[{\"name\":\"Diesel FuelSave\",\"price\":1.239,\"type\":\"DIESEL\"},{\"name\":\"V-Power Diesel\"," +
                    "\"price\":1.429,\"type\":\"DIESEL_PREMIUM\"},{\"name\":\"V-Power Racing\",\"price\":1.599,\"type\":\"RON100\"},{\"name\":\"Super FuelSave E10\"," +
                    "\"price\":1.389,\"type\":\"E10_RON98\"},{\"name\":\"Super FuelSave 95\",\"price\":1.409,\"type\":\"E5\"}]','EUR',1573545852000," +
                    "'[\"MASTER_CARD\",\"VISA\",\"CASH\",\"AMERICAN_EXPRESS\",\"DINERS_CLUB\",\"EUROSHELL\",\"PAYPAL\"]',0,'[\"TOILET\",\"SHOP\"]','[]','[\"SHELL_CLUBSMART\"]','[]'," +
                    "'[\"CAR_WASH\",\"TYRE_AIR\",\"VACUUM\",\"SCREEN_WASH_WATER\"]','[\"AD_BLUE\"]',0,1573547242518,'dab084a3-2012-427e-aff7-85c245cd0f1b'," +
                    "'[{\"commandType\":\"MOVETO\",\"locationPoint\":{\"lat\":48.935219700863996,\"lon\":8.376566916704178}}]','DE','Ettlingen','76275',NULL,NULL,'Nobelstr.','24'," +
                    "'c=DE;l=Ettlingen;pc=76275;s=Nobelstr.;hn=24');"
            )

            close()
        }

        // Open latest version of the database. Room will validate the schema
        // once all migrations execute.
        Room.databaseBuilder(InstrumentationRegistry.getInstrumentation().targetContext, POIKitDatabase::class.java, TEST_DB).addMigrations(
            POIKitDatabase.migration1to2,
            POIKitDatabase.migration2to3,
            POIKitDatabase.migration3to4,
            POIKitDatabase.migration4to5,
            POIKitDatabase.migration5to6,
            POIKitDatabase.migration6to7
            // Add all migrations here
        ).build()
    }

    @Test
    @Throws(IOException::class)
    fun migrate1To2() {
        helper.createDatabase(TEST_DB, 1).apply {
            execSQL(
                "INSERT INTO \"GasStation\" VALUES ('Shell','[{\"days\":[\"MONDAY\",\"TUESDAY\",\"WEDNESDAY\",\"THURSDAY\",\"FRIDAY\",\"SATURDAY\",\"SUNDAY\"]," +
                    "\"hours\":[{\"from\":\"0\",\"to\":\"0\"}],\"rule\":\"OPEN\"}]','[{\"name\":\"Diesel FuelSave\",\"price\":1.249,\"type\":\"DIESEL\"},{\"name\":\"V-Power Diesel\"," +
                    "\"price\":1.439,\"type\":\"DIESEL_PREMIUM\"},{\"name\":\"V-Power Racing\",\"price\":1.609,\"type\":\"RON100\"}," +
                    "{\"name\":\"Super FuelSave E10\",\"price\":1.399,\"type\":\"E10_RON98\"},{\"name\":\"Super FuelSave 95\",\"price\":1.419,\"type\":\"E5\"}]','EUR',1573545854000," +
                    "'[\"MASTER_CARD\",\"VISA\",\"CASH\",\"AMERICAN_EXPRESS\",\"DINERS_CLUB\",\"EUROSHELL\",\"PAYPAL\"]',0,'[\"TOILET\",\"SHOP\",\"ATM\"]','[\"BAKERY\",\"CAFE\"]'," +
                    "'[\"SHELL_CLUBSMART\"]','[]','[\"CAR_WASH\",\"TYRE_AIR\",\"VACUUM\",\"SCREEN_WASH_WATER\"]','[\"AD_BLUE\"]',0,1573547242518,'496a4123-4acc-4f03-9488-bc85a76c41aa'," +
                    "'[{\"commandType\":\"MOVETO\",\"locationPoint\":{\"lat\":48.935194152219424,\"lon\":8.42889279127121}}]','DE','Ettlingen','76275',NULL,NULL,'Pforzheimer Str.','110-116'," +
                    "'c=DE;l=Ettlingen;pc=76275;s=Pforzheimer Str.;hn=110-116');"
            )
            execSQL(
                "INSERT INTO \"GasStation\" VALUES ('Shell','[{\"days\":[\"MONDAY\",\"TUESDAY\",\"WEDNESDAY\",\"THURSDAY\",\"FRIDAY\",\"SATURDAY\",\"SUNDAY\"]," +
                    "\"hours\":[{\"from\":\"0\",\"to\":\"0\"}],\"rule\":\"OPEN\"}]','[{\"name\":\"Diesel FuelSave\",\"price\":1.239,\"type\":\"DIESEL\"},{\"name\":\"V-Power Diesel\"," +
                    "\"price\":1.429,\"type\":\"DIESEL_PREMIUM\"},{\"name\":\"V-Power Racing\",\"price\":1.599,\"type\":\"RON100\"},{\"name\":\"Super FuelSave E10\"," +
                    "\"price\":1.389,\"type\":\"E10_RON98\"},{\"name\":\"Super FuelSave 95\",\"price\":1.409,\"type\":\"E5\"}]','EUR',1573545852000," +
                    "'[\"MASTER_CARD\",\"VISA\",\"CASH\",\"AMERICAN_EXPRESS\",\"DINERS_CLUB\",\"EUROSHELL\",\"PAYPAL\"]',0,'[\"TOILET\",\"SHOP\"]','[]','[\"SHELL_CLUBSMART\"]','[]'," +
                    "'[\"CAR_WASH\",\"TYRE_AIR\",\"VACUUM\",\"SCREEN_WASH_WATER\"]','[\"AD_BLUE\"]',0,1573547242518,'dab084a3-2012-427e-aff7-85c245cd0f1b'," +
                    "'[{\"commandType\":\"MOVETO\",\"locationPoint\":{\"lat\":48.935219700863996,\"lon\":8.376566916704178}}]','DE','Ettlingen','76275',NULL,NULL,'Nobelstr.','24'," +
                    "'c=DE;l=Ettlingen;pc=76275;s=Nobelstr.;hn=24');"
            )

            // Prepare for the next version.
            close()
        }

        val db = helper.runMigrationsAndValidate(TEST_DB, 2, true, POIKitDatabase.migration1to2)

        // MigrationTestHelper automatically verifies the schema changes,
        // but you need to validate that the data was migrated properly.
        val cursor = db.query("SELECT * FROM GasStation")

        assertEquals(2, cursor.count)

        // Check if lat / lon migration succeeded
        assertEquals(true, cursor.moveToFirst())

        assertEquals(48.935194152219424, cursor.getDouble(cursor.getColumnIndex("latitude")), 0.0001)
        assertEquals(8.42889279127121, cursor.getDouble(cursor.getColumnIndex("longitude")), 0.0001)
    }

    @Test
    @Throws(IOException::class)
    fun migrate2To3() {
        helper.createDatabase(TEST_DB, 2).apply {
            execSQL(
                "INSERT INTO \"GasStation\" VALUES ('Shell','[{\"days\":[\"MONDAY\",\"TUESDAY\",\"WEDNESDAY\",\"THURSDAY\",\"FRIDAY\",\"SATURDAY\",\"SUNDAY\"]," +
                    "\"hours\":[{\"from\":\"0\",\"to\":\"0\"}],\"rule\":\"OPEN\"}]','[{\"name\":\"Diesel FuelSave\",\"price\":1.249,\"type\":\"DIESEL\"}," +
                    "{\"name\":\"V-Power Diesel\",\"price\":1.439,\"type\":\"DIESEL_PREMIUM\"},{\"name\":\"V-Power Racing\",\"price\":1.609,\"type\":\"RON100\"}," +
                    "{\"name\":\"Super FuelSave E10\",\"price\":1.399,\"type\":\"E10_RON98\"},{\"name\":\"Super FuelSave 95\",\"price\":1.419,\"type\":\"E5\"}]','EUR',1573545854000," +
                    "'[\"MASTER_CARD\",\"VISA\",\"CASH\",\"AMERICAN_EXPRESS\",\"DINERS_CLUB\",\"EUROSHELL\",\"PAYPAL\"]',0,'[\"TOILET\",\"SHOP\",\"ATM\"]','[\"BAKERY\",\"CAFE\"]'," +
                    "'[\"SHELL_CLUBSMART\"]','[]','[\"CAR_WASH\",\"TYRE_AIR\",\"VACUUM\",\"SCREEN_WASH_WATER\"]','[\"AD_BLUE\"]',0,1573547242518, 48.935194152219424, 8.42889279127121, " +
                    "'496a4123-4acc-4f03-9488-bc85a76c41aa','[{\"commandType\":\"MOVETO\",\"locationPoint\":{\"lat\":48.935194152219424,\"lon\":8.42889279127121}}]','DE','Ettlingen','76275'," +
                    "NULL,NULL,'Pforzheimer Str.','110-116','c=DE;l=Ettlingen;pc=76275;s=Pforzheimer Str.;hn=110-116');"
            )
            execSQL(
                "INSERT INTO \"GasStation\" VALUES ('Shell','[{\"days\":[\"MONDAY\",\"TUESDAY\",\"WEDNESDAY\",\"THURSDAY\",\"FRIDAY\",\"SATURDAY\",\"SUNDAY\"]," +
                    "\"hours\":[{\"from\":\"0\",\"to\":\"0\"}],\"rule\":\"OPEN\"}]','[{\"name\":\"Diesel FuelSave\",\"price\":1.239,\"type\":\"DIESEL\"},{\"name\":\"V-Power Diesel\"," +
                    "\"price\":1.429,\"type\":\"DIESEL_PREMIUM\"},{\"name\":\"V-Power Racing\",\"price\":1.599,\"type\":\"RON100\"},{\"name\":\"Super FuelSave E10\"," +
                    "\"price\":1.389,\"type\":\"E10_RON98\"},{\"name\":\"Super FuelSave 95\",\"price\":1.409,\"type\":\"E5\"}]','EUR',1573545852000," +
                    "'[\"MASTER_CARD\",\"VISA\",\"CASH\",\"AMERICAN_EXPRESS\",\"DINERS_CLUB\",\"EUROSHELL\",\"PAYPAL\"]',0,'[\"TOILET\",\"SHOP\"]','[]','[\"SHELL_CLUBSMART\"]','[]'," +
                    "'[\"CAR_WASH\",\"TYRE_AIR\",\"VACUUM\",\"SCREEN_WASH_WATER\"]','[\"AD_BLUE\"]',0,1573547242518, 48.935219700863996, 8.376566916704178, " +
                    "'dab084a3-2012-427e-aff7-85c245cd0f1b','[{\"commandType\":\"MOVETO\",\"locationPoint\":{\"lat\":48.935219700863996,\"lon\":8.376566916704178}}]','DE','Ettlingen'," +
                    "'76275',NULL,NULL,'Nobelstr.','24','c=DE;l=Ettlingen;pc=76275;s=Nobelstr.;hn=24');"
            )

            // Prepare for the next version.
            close()
        }

        val db = helper.runMigrationsAndValidate(TEST_DB, 3, true, POIKitDatabase.migration2to3)

        val cursor = db.query("SELECT * FROM GasStation")
        assertEquals(2, cursor.count)

        assertEquals(true, cursor.moveToFirst())
        assertEquals(null, cursor.getString(cursor.getColumnIndex("priceFormat")))
    }

    @Test
    @Throws(IOException::class)
    fun migrate3To4() {
        helper.createDatabase(TEST_DB, 3).apply {
            execSQL(
                "INSERT INTO \"GasStation\" VALUES ('Shell','[{\"days\":[\"MONDAY\",\"TUESDAY\",\"WEDNESDAY\",\"THURSDAY\",\"FRIDAY\",\"SATURDAY\",\"SUNDAY\"]," +
                    "\"hours\":[{\"from\":\"0\",\"to\":\"0\"}],\"rule\":\"OPEN\"}]','[{\"name\":\"Diesel FuelSave\",\"price\":1.249,\"type\":\"DIESEL\"}," +
                    "{\"name\":\"V-Power Diesel\",\"price\":1.439,\"type\":\"DIESEL_PREMIUM\"},{\"name\":\"V-Power Racing\",\"price\":1.609,\"type\":\"RON100\"}," +
                    "{\"name\":\"Super FuelSave E10\",\"price\":1.399,\"type\":\"E10_RON98\"},{\"name\":\"Super FuelSave 95\",\"price\":1.419,\"type\":\"E5\"}]','EUR', 'd.dd', 1573545854000," +
                    "'[\"MASTER_CARD\",\"VISA\",\"CASH\",\"AMERICAN_EXPRESS\",\"DINERS_CLUB\",\"EUROSHELL\",\"PAYPAL\"]',0,'[\"TOILET\",\"SHOP\",\"ATM\"]','[\"BAKERY\",\"CAFE\"]'," +
                    "'[\"SHELL_CLUBSMART\"]','[]','[\"CAR_WASH\",\"TYRE_AIR\",\"VACUUM\",\"SCREEN_WASH_WATER\"]','[\"AD_BLUE\"]',0,1573547242518, 48.935194152219424, 8.42889279127121, " +
                    "'496a4123-4acc-4f03-9488-bc85a76c41aa','[{\"commandType\":\"MOVETO\",\"locationPoint\":{\"lat\":48.935194152219424,\"lon\":8.42889279127121}}]','DE','Ettlingen','76275'," +
                    "NULL,NULL,'Pforzheimer Str.','110-116','c=DE;l=Ettlingen;pc=76275;s=Pforzheimer Str.;hn=110-116');"
            )
            execSQL(
                "INSERT INTO \"GasStation\" VALUES ('Shell','[{\"days\":[\"MONDAY\",\"TUESDAY\",\"WEDNESDAY\",\"THURSDAY\",\"FRIDAY\",\"SATURDAY\",\"SUNDAY\"]," +
                    "\"hours\":[{\"from\":\"0\",\"to\":\"0\"}],\"rule\":\"OPEN\"}]','[{\"name\":\"Diesel FuelSave\",\"price\":1.239,\"type\":\"DIESEL\"}," +
                    "{\"name\":\"V-Power Diesel\",\"price\":1.429,\"type\":\"DIESEL_PREMIUM\"},{\"name\":\"V-Power Racing\",\"price\":1.599,\"type\":\"RON100\"}," +
                    "{\"name\":\"Super FuelSave E10\",\"price\":1.389,\"type\":\"E10_RON98\"},{\"name\":\"Super FuelSave 95\",\"price\":1.409,\"type\":\"E5\"}]','EUR', 'd.dds', 1573545852000," +
                    "'[\"MASTER_CARD\",\"VISA\",\"CASH\",\"AMERICAN_EXPRESS\",\"DINERS_CLUB\",\"EUROSHELL\",\"PAYPAL\"]',0,'[\"TOILET\",\"SHOP\"]','[]','[\"SHELL_CLUBSMART\"]','[]'," +
                    "'[\"CAR_WASH\",\"TYRE_AIR\",\"VACUUM\",\"SCREEN_WASH_WATER\"]','[\"AD_BLUE\"]',0,1573547242518, 48.935219700863996, 8.376566916704178,'dab084a3-2012-427e-aff7-85c245cd0f1b'," +
                    "'[{\"commandType\":\"MOVETO\",\"locationPoint\":{\"lat\":48.935219700863996,\"lon\":8.376566916704178}}]','DE','Ettlingen','76275',NULL,NULL," +
                    "'Nobelstr.','24','c=DE;l=Ettlingen;pc=76275;s=Nobelstr.;hn=24');"
            )

            // Prepare for the next version.
            close()
        }

        val db = helper.runMigrationsAndValidate(TEST_DB, 4, true, POIKitDatabase.migration3to4)

        val cursor = db.query("SELECT * FROM GasStation")
        assertEquals(2, cursor.count)
        assertEquals(29, cursor.columnCount)

        assertEquals(true, cursor.moveToFirst())
        assertEquals(null, cursor.getString(cursor.getColumnIndex("brand")))
    }

    @Test
    @Throws(IOException::class)
    fun migrate4to5() {
        helper.createDatabase(TEST_DB, 4).apply {
            execSQL(
                "INSERT INTO \"GasStation\" VALUES ('Shell','[{\"days\":[\"MONDAY\",\"TUESDAY\",\"WEDNESDAY\",\"THURSDAY\",\"FRIDAY\",\"SATURDAY\",\"SUNDAY\"]," +
                    "\"hours\":[{\"from\":\"0\",\"to\":\"0\"}],\"rule\":\"OPEN\"}]','[{\"name\":\"Diesel FuelSave\",\"price\":1.249,\"type\":\"DIESEL\"}," +
                    "{\"name\":\"V-Power Diesel\",\"price\":1.439,\"type\":\"DIESEL_PREMIUM\"},{\"name\":\"V-Power Racing\",\"price\":1.609,\"type\":\"RON100\"}," +
                    "{\"name\":\"Super FuelSave E10\",\"price\":1.399,\"type\":\"E10_RON98\"},{\"name\":\"Super FuelSave 95\",\"price\":1.419,\"type\":\"E5\"}]','EUR', 'd.dd', 1573545854000," +
                    "'[\"MASTER_CARD\",\"VISA\",\"CASH\",\"AMERICAN_EXPRESS\",\"DINERS_CLUB\",\"EUROSHELL\",\"PAYPAL\"]',0,'[\"TOILET\",\"SHOP\",\"ATM\"]','[\"BAKERY\",\"CAFE\"]'," +
                    "'[\"SHELL_CLUBSMART\"]','[]','[\"CAR_WASH\",\"TYRE_AIR\",\"VACUUM\",\"SCREEN_WASH_WATER\"]','[\"AD_BLUE\"]', 0,0,1573547242518, 48.935194152219424, 8.42889279127121, " +
                    "'496a4123-4acc-4f03-9488-bc85a76c41aa','[{\"commandType\":\"MOVETO\",\"locationPoint\":{\"lat\":48.935194152219424,\"lon\":8.42889279127121}}]','DE','Ettlingen','76275'," +
                    "NULL,NULL,'Pforzheimer Str.','110-116','c=DE;l=Ettlingen;pc=76275;s=Pforzheimer Str.;hn=110-116');"
            )
            execSQL(
                "INSERT INTO \"GasStation\" VALUES ('Shell','[{\"days\":[\"MONDAY\",\"TUESDAY\",\"WEDNESDAY\",\"THURSDAY\",\"FRIDAY\",\"SATURDAY\",\"SUNDAY\"]," +
                    "\"hours\":[{\"from\":\"0\",\"to\":\"0\"}],\"rule\":\"OPEN\"}]','[{\"name\":\"Diesel FuelSave\",\"price\":1.239,\"type\":\"DIESEL\"}," +
                    "{\"name\":\"V-Power Diesel\",\"price\":1.429,\"type\":\"DIESEL_PREMIUM\"},{\"name\":\"V-Power Racing\",\"price\":1.599,\"type\":\"RON100\"}," +
                    "{\"name\":\"Super FuelSave E10\",\"price\":1.389,\"type\":\"E10_RON98\"},{\"name\":\"Super FuelSave 95\",\"price\":1.409,\"type\":\"E5\"}]','EUR', 'd.dds', 1573545852000," +
                    "'[\"MASTER_CARD\",\"VISA\",\"CASH\",\"AMERICAN_EXPRESS\",\"DINERS_CLUB\",\"EUROSHELL\",\"PAYPAL\"]',0,'[\"TOILET\",\"SHOP\"]','[]'," +
                    "'[\"SHELL_CLUBSMART\"]','[]','[\"CAR_WASH\",\"TYRE_AIR\",\"VACUUM\",\"SCREEN_WASH_WATER\"]','[\"AD_BLUE\"]', 0,0,1573547242518, 48.935219700863996, 8.376566916704178," +
                    "'dab084a3-2012-427e-aff7-85c245cd0f1b','[{\"commandType\":\"MOVETO\",\"locationPoint\":{\"lat\":48.935219700863996,\"lon\":8.376566916704178}}]','DE','Ettlingen','76275'," +
                    "NULL,NULL,'Nobelstr.','24','c=DE;l=Ettlingen;pc=76275;s=Nobelstr.;hn=24');"
            )

            // Prepare for the next version.
            close()
        }

        val db = helper.runMigrationsAndValidate(TEST_DB, 5, true, POIKitDatabase.migration4to5)

        val cursor = db.query("SELECT * FROM GasStation")
        assertEquals(2, cursor.count)
        assertEquals(30, cursor.columnCount)

        assertEquals(true, cursor.moveToFirst())
        assertEquals(null, cursor.getString(cursor.getColumnIndex("priceComparisonOptOut")))
    }

    @Test
    @Throws(IOException::class)
    fun migrate5to6() {
        helper.createDatabase(TEST_DB, 5).apply {
            execSQL(
                "INSERT INTO \"GasStation\" (\"name\", \"brand\", \"openingHours\", \"prices\", \"currency\", \"priceFormat\", \"validFrom\", \"paymentMethods\", " +
                    "\"isConnectedFuelingAvailable\", \"amenities\", \"foods\", \"loyaltyPrograms\", \"postalServices\", \"services\", \"shopGoods\", \"priceComparisonOptOut\", \"temporary\", " +
                    "\"updatedAt\", \"latitude\", \"longitude\", \"id\", \"geometry\", \"countryCode\", \"city\", \"postalCode\", \"suburb\", \"state\", \"street\", \"houseNumber\", " +
                    "\"encodedAddress\") VALUES ('Shell', 'Shell', '[{\"days\":[\"MONDAY\",\"TUESDAY\",\"WEDNESDAY\",\"THURSDAY\",\"FRIDAY\",\"SATURDAY\",\"SUNDAY\"]," +
                    "\"hours\":[{\"from\":\"6\",\"to\":\"22\"}],\"rule\":\"OPEN\"}]', '[{\"name\":\"Diesel FuelSave\",\"price\":1.329,\"type\":\"DIESEL\"},{\"name\":\"V-Power Racing\"," +
                    "\"price\":1.739,\"type\":\"RON100\"},{\"name\":\"Super FuelSave E10\",\"price\":1.489,\"type\":\"E10\"},{\"name\":\"Super FuelSave 95\",\"price\":1.549,\"type\":\"E5\"}]', " +
                    "'EUR', 'd.dds', '1619679925000', '[\"CASH\",\"EUROSHELL\",\"MASTER_CARD\",\"PAYPAL\",\"VISA\"]', '0', '[\"SHOP\",\"TOILET\"]', '[\"CAFE\"]', '[\"SHELL_CLUBSMART\"]', '[]', " +
                    "'[\"CAR_WASH\",\"SCREEN_WASH_WATER\",\"TYRE_AIR\"]', '[\"AD_BLUE\"]', '0', '0', '1619683525520', '52.5563160654065', '13.4150576591492', " +
                    "'a041f06f-f60d-49cc-a99a-def2f0a104f0', '[{\"commandType\":\"MOVETO\",\"locationPoint\":{\"lat\":52.55631606540652,\"lon\":13.41505765914917}}]', 'DE', 'Berlin', '13189', " +
                    "'', '', 'Berliner Str. 83 Eschengraben', '', 'c=DE;l=Berlin;pc=13189;s=Berliner Str. 83 Eschengraben');"
            )
            execSQL(
                "INSERT INTO \"GasStation\" (\"name\", \"brand\", \"openingHours\", \"prices\", \"currency\", \"priceFormat\", \"validFrom\", \"paymentMethods\", " +
                    "\"isConnectedFuelingAvailable\", \"amenities\", \"foods\", \"loyaltyPrograms\", \"postalServices\", \"services\", \"shopGoods\", \"priceComparisonOptOut\", \"temporary\", " +
                    "\"updatedAt\", \"latitude\", \"longitude\", \"id\", \"geometry\", \"countryCode\", \"city\", \"postalCode\", \"suburb\", \"state\", \"street\", \"houseNumber\", " +
                    "\"encodedAddress\") VALUES ('Elan', 'Elan', '[{\"days\":[\"MONDAY\",\"TUESDAY\",\"WEDNESDAY\",\"THURSDAY\",\"FRIDAY\",\"SATURDAY\",\"SUNDAY\"]," +
                    "\"hours\":[{\"from\":\"0\",\"to\":\"0\"}],\"rule\":\"OPEN\"}]', '[{\"name\":\"Diesel\",\"price\":1.299,\"type\":\"DIESEL\"},{\"name\":\"Super E10\",\"price\":1.459," +
                    "\"type\":\"E10\"},{\"name\":\"Super 95\",\"price\":1.519,\"type\":\"E5\"}]', 'EUR', 'd.dds', '1619681691000', " +
                    "'[\"AMERICAN_EXPRESS\",\"APPLE_PAY\",\"DKV\",\"FF_CARD\",\"GIROCARD\",\"GOOGLE_PAY\",\"MAESTRO\",\"MASTER_CARD\",\"UTA\",\"V_PAY\",\"VISA\",\"WESTFALEN_CARD\"]', '0', " +
                    "'[\"TRUCK_SUITABLE\"]', '[\"CAFE\",\"TAKE_AWAY\"]', '[]', '[]', '[\"CAR_WASH\",\"GAS_BOTTLE_REFILL\",\"TWENTY_FOUR_HOURS_FUELING\"]', '[]', '0', '0', '1619683525520', " +
                    "'52.5584098766817', '13.4327495098114', '0cd0782f-2a41-4c99-8b34-f5ad02a07f52', " +
                    "'[{\"commandType\":\"MOVETO\",\"locationPoint\":{\"lat\":52.55840987668165,\"lon\":13.432749509811401}}]', 'DE', 'Berlin', '13089', '', '', 'Tino-Schwierzina-Str', '37', " +
                    "'c=DE;l=Berlin;pc=13089;s=Tino-Schwierzina-Str;hn=37');"
            )

            // Prepare for the next version.
            close()
        }

        val db = helper.runMigrationsAndValidate(TEST_DB, 6, true, POIKitDatabase.migration5to6)

        val cursor = db.query("SELECT * FROM GasStation")
        assertEquals(2, cursor.count)
        assertEquals(31, cursor.columnCount)

        assertEquals(true, cursor.moveToFirst())
        assertEquals("", cursor.getString(cursor.getColumnIndex("cofuPaymentMethods")))
    }

    @Test
    @Throws(IOException::class)
    fun migrate6to7() {
        helper.createDatabase(TEST_DB, 6).apply {
            execSQL(
                "INSERT INTO \"GasStation\" (\"name\", \"brand\", \"openingHours\", \"prices\", \"currency\", \"priceFormat\", \"validFrom\", \"paymentMethods\", " +
                    "\"isConnectedFuelingAvailable\", \"amenities\", \"foods\", \"loyaltyPrograms\", \"postalServices\", \"services\", \"shopGoods\", \"priceComparisonOptOut\", " +
                    "\"temporary\", \"updatedAt\", \"latitude\", \"longitude\", \"id\", \"geometry\", \"countryCode\", \"city\", \"postalCode\", \"suburb\", \"state\", \"street\", " +
                    "\"houseNumber\", \"encodedAddress\", \"cofuPaymentMethods\") VALUES ('Shell', 'Shell', " +
                    "'[{\"days\":[\"MONDAY\",\"TUESDAY\",\"WEDNESDAY\",\"THURSDAY\",\"FRIDAY\",\"SATURDAY\",\"SUNDAY\"],\"hours\":[{\"from\":\"6\",\"to\":\"22\"}],\"rule\":\"OPEN\"}]', " +
                    "'[{\"name\":\"Diesel FuelSave\",\"price\":1.329,\"type\":\"DIESEL\"},{\"name\":\"V-Power Racing\",\"price\":1.739,\"type\":\"RON100\"}," +
                    "{\"name\":\"Super FuelSave E10\",\"price\":1.489,\"type\":\"E10\"},{\"name\":\"Super FuelSave 95\",\"price\":1.549,\"type\":\"E5\"}]', 'EUR', 'd.dds', '1619679925000', " +
                    "'[\"CASH\",\"EUROSHELL\",\"MASTER_CARD\",\"PAYPAL\",\"VISA\"]', '0', '[\"SHOP\",\"TOILET\"]', '[\"CAFE\"]', '[\"SHELL_CLUBSMART\"]', '[]', " +
                    "'[\"CAR_WASH\",\"SCREEN_WASH_WATER\",\"TYRE_AIR\"]', '[\"AD_BLUE\"]', '0', '0', '1619683525520', '52.5563160654065', '13.4150576591492', " +
                    "'a041f06f-f60d-49cc-a99a-def2f0a104f0', '[{\"commandType\":\"MOVETO\",\"locationPoint\":{\"lat\":52.55631606540652,\"lon\":13.41505765914917}}]', 'DE', 'Berlin', " +
                    "'13189', '', '', 'Berliner Str. 83 Eschengraben', '', 'c=DE;l=Berlin;pc=13189;s=Berliner Str. 83 Eschengraben', " +
                    "'[\"cofu:creditcard\",\"cofu:paypal\",\"cofu:applepay\",\"cofu:giropay\",\"cofu:dkv\"]');"
            )
            execSQL(
                "INSERT INTO \"GasStation\" (\"name\", \"brand\", \"openingHours\", \"prices\", \"currency\", \"priceFormat\", \"validFrom\", \"paymentMethods\", " +
                    "\"isConnectedFuelingAvailable\", \"amenities\", \"foods\", \"loyaltyPrograms\", \"postalServices\", \"services\", \"shopGoods\", \"priceComparisonOptOut\", " +
                    "\"temporary\", \"updatedAt\", \"latitude\", \"longitude\", \"id\", \"geometry\", \"countryCode\", \"city\", \"postalCode\", \"suburb\", \"state\", \"street\", " +
                    "\"houseNumber\", \"encodedAddress\", \"cofuPaymentMethods\") VALUES ('Elan', 'Elan', " +
                    "'[{\"days\":[\"MONDAY\",\"TUESDAY\",\"WEDNESDAY\",\"THURSDAY\",\"FRIDAY\",\"SATURDAY\",\"SUNDAY\"],\"hours\":[{\"from\":\"0\",\"to\":\"0\"}],\"rule\":\"OPEN\"}]', " +
                    "'[{\"name\":\"Diesel\",\"price\":1.299,\"type\":\"DIESEL\"},{\"name\":\"Super E10\",\"price\":1.459,\"type\":\"E10\"}," +
                    "{\"name\":\"Super 95\",\"price\":1.519,\"type\":\"E5\"}]', 'EUR', 'd.dds', '1619681691000', " +
                    "'[\"AMERICAN_EXPRESS\",\"APPLE_PAY\",\"DKV\",\"FF_CARD\",\"GIROCARD\",\"GOOGLE_PAY\",\"MAESTRO\",\"MASTER_CARD\",\"UTA\",\"V_PAY\",\"VISA\",\"WESTFALEN_CARD\"]', '0', " +
                    "'[\"TRUCK_SUITABLE\"]', '[\"CAFE\",\"TAKE_AWAY\"]', '[]', '[]', '[\"CAR_WASH\",\"GAS_BOTTLE_REFILL\",\"TWENTY_FOUR_HOURS_FUELING\"]', '[]', '0', '0', '1619683525520', " +
                    "'52.5584098766817', '13.4327495098114', '0cd0782f-2a41-4c99-8b34-f5ad02a07f52', " +
                    "'[{\"commandType\":\"MOVETO\",\"locationPoint\":{\"lat\":52.55840987668165,\"lon\":13.432749509811401}}]', 'DE', 'Berlin', '13089', '', '', 'Tino-Schwierzina-Str', '37', " +
                    "'c=DE;l=Berlin;pc=13089;s=Tino-Schwierzina-Str;hn=37', " +
                    "'[\"cofu:giropay\",\"cofu:applepay\",\"cofu:creditcard\",\"cofu:dkv\",\"cofu:hoyer\",\"cofu:sepa\",\"cofu:zgm\",\"cofu:paypal\"]');"
            )

            // Prepare for the next version.
            close()
        }

        val db = helper.runMigrationsAndValidate(TEST_DB, 7, true, POIKitDatabase.migration6to7)

        val cursor = db.query("SELECT * FROM GasStation")
        assertEquals(2, cursor.count)
        assertEquals(32, cursor.columnCount)

        assertEquals(true, cursor.moveToFirst())
        assertEquals(null, cursor.getString(cursor.getColumnIndex("isOnlineCoFuGasStation")))
    }
}
