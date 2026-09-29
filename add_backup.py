import os

with open('app/src/main/java/com/tools/warrantybox/ui/screens/ListScreen.kt', 'r') as f:
    text = f.read()

imports = """
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.launch
import java.io.InputStreamReader
import java.io.OutputStreamWriter
"""

if 'import android.net.Uri' not in text:
    text = text.replace('import androidx.compose.foundation.background', imports + '\nimport androidx.compose.foundation.background')

backup_logic = """
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    
    val exportLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri: Uri? ->
        uri?.let {
            coroutineScope.launch {
                try {
                    val outputStream = context.contentResolver.openOutputStream(it)
                    val writer = OutputStreamWriter(outputStream)
                    val json = Gson().toJson(items)
                    writer.write(json)
                    writer.close()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }
    
    val importLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
        uri?.let {
            coroutineScope.launch {
                try {
                    val inputStream = context.contentResolver.openInputStream(it)
                    val reader = InputStreamReader(inputStream)
                    val itemType = object : TypeToken<List<WarrantyItem>>() {}.type
                    val importedItems: List<WarrantyItem> = Gson().fromJson(reader, itemType)
                    reader.close()
                    viewModel.replaceAll(importedItems)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }
"""

if 'val context = LocalContext.current' not in text:
    text = text.replace('var selectedTabIndex by remember { mutableStateOf(0) }', backup_logic + '\n    var selectedTabIndex by remember { mutableStateOf(0) }')

    text = text.replace('// Backup logic', 'exportLauncher.launch("warranty_backup.json")')
    text = text.replace('// Restore logic', 'importLauncher.launch(arrayOf("application/json"))')

with open('app/src/main/java/com/tools/warrantybox/ui/screens/ListScreen.kt', 'w') as f:
    f.write(text)

print("done")
