import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.re_mind.android.db.GeofenceEntity

@Entity(tableName = "reminders",
    foreignKeys = [
        ForeignKey(
            entity = GeofenceEntity::class,
            parentColumns = ["id"],
            childColumns = ["geofenceId"],
            onDelete = ForeignKey.CASCADE // If a geofence is deleted, delete associated reminders
        )
    ],
    indices = [Index(value = ["geofenceId"])])
data class ReminderEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val geofenceId: String, // Foreign key referencing GeofenceLocationEntity
    val message: String,
    val creationTimeMillis: Long = System.currentTimeMillis(),
    var triggerTimeMillis: Long? = null, // When the reminder was actually triggered
    var isEnabled: Boolean = true,
    var isTriggered: Boolean = false
)