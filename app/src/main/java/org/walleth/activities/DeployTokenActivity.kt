package org.walleth.activities

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import com.github.salomonbrys.kodein.LazyKodein
import com.github.salomonbrys.kodein.android.appKodein
import com.github.salomonbrys.kodein.instance
import kotlinx.android.synthetic.main.activity_account_edit.*
import kotlinx.android.synthetic.main.activity_deploy_token.*
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.launch
import org.ethereum.geth.Geth
import org.ligi.kaxtui.alert
import org.walleth.R
import org.walleth.activities.qrscan.startScanActivityForResult
import org.walleth.data.AppDatabase
import org.walleth.data.keystore.WallethKeyStore


fun Context.getDeployIntent() = Intent(this, DeployTokenActivity::class.java).apply {

}

private const val READ_REQUEST_CODE = 42
const val EXTRA_ADDRESS = "ADDRESS"
const val EXTRA_NAME = "NAME"

class DeployTokenActivity : AppCompatActivity() {

    private val keyStore: WallethKeyStore by LazyKodein(appKodein).instance()
    private val appDatabase: AppDatabase by LazyKodein(appKodein).instance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_deploy_token)

        supportActionBar?.subtitle = getString(R.string.deploy_token_subtitle)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        fab.setOnClickListener {
            val alertBuilder = AlertDialog.Builder(this)

            launch(UI) {
                try {
                    val transactOpts = null
                    val parsedAbiJson = ""
                    val byteCode = null
                    val ethClient = null
                    val interfaces = null
                    val contract = async {
                        Geth.deployContract(transactOpts, parsedAbiJson, byteCode, ethClient, interfaces)
                    }.await()
                    alertBuilder
                            .setMessage(getString(R.string.deployed_token_alert_message, contract.address))
                            .setTitle(getString(R.string.dialog_title_success))
                            .setPositiveButton(android.R.string.ok) { dialogInterface: DialogInterface, i: Int ->
                                setResult(Activity.RESULT_OK, Intent().apply {
                                    putExtra(EXTRA_ADDRESS, contract.address.hex)
                                    putExtra(EXTRA_NAME, contract_name.text)
                                })
                                finish()
                            }.show()


                } catch (e: Exception) {

                    alertBuilder
                            .setMessage(e.message)
                            .setTitle(getString(R.string.dialog_title_error))
                            .show()
                }
            }
        }
    }


    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_import, menu)
        menu.findItem(R.id.menu_open).isVisible = Build.VERSION.SDK_INT >= 19
        return super.onCreateOptionsMenu(menu)
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int,
                                         resultData: Intent?) {


        resultData?.let {
            if (it.hasExtra("SCAN_RESULT")) {
                contract_abi.setText(it.getStringExtra("SCAN_RESULT"))
            }
            if (requestCode == READ_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
                contract_abi.setText(readTextFromUri(it.data))
            }
        }


    }

    private fun readTextFromUri(uri: Uri) = contentResolver.openInputStream(uri).reader().readText()


    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {

        R.id.menu_open -> {
            try {
                val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
                intent.addCategory(Intent.CATEGORY_OPENABLE)
                intent.type = "*/*"

                startActivityForResult(intent, READ_REQUEST_CODE)
            } catch (e: ActivityNotFoundException) {
                alert(R.string.saf_activity_not_found_problem)
            }
            true
        }

        R.id.menu_scan -> {
            startScanActivityForResult(this)
            true
        }

        android.R.id.home -> {
            finish()
            true
        }
        else -> super.onOptionsItemSelected(item)
    }
}
