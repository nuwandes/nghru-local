package org.southasia.ghru.repository

import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import org.southasia.ghru.R
import org.southasia.ghru.util.LocaleManager
import org.southasia.ghru.vo.HomeItem
import org.southasia.ghru.vo.Message
import org.southasia.ghru.vo.Resource
import org.southasia.ghru.vo.Status
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class HomeRepository @Inject constructor(
    private val context: Context,
    private val localeManager: LocaleManager
) {

    fun getHomeItems(): LiveData<Resource<List<HomeItem>>> {


        val mHomeItem = HomeItem(
            1,
            getStringByLocalBefore17(context, R.string.screening_register_participant, localeManager.getLanguage()),
            R.drawable.ic_icon_register_patient
        )

        val mHomeItem1 = HomeItem(
            2,
            getStringByLocalBefore17(context, R.string.screening_blood_pressure, localeManager.getLanguage()),
            R.drawable.ic_icon_bp
        )

        val mHomeItem2 = HomeItem(
            3,
            getStringByLocalBefore17(context, R.string.screening_body_measurements, localeManager.getLanguage()),
            R.drawable.ic_icon_body_measurements
        )

        val mHomeItem3 = HomeItem(
            4,
            getStringByLocalBefore17(context, R.string.screening_biological_samples, localeManager.getLanguage()),
            R.drawable.ic_icon_bio_samples
        )

        val mHomeItem4 = HomeItem(
            5,
            getStringByLocalBefore17(context, R.string.ecg, localeManager.getLanguage()),
            R.drawable.ic_icon_ecg
        )

        val mHomeItem5 = HomeItem(
            6,
            getStringByLocalBefore17(context, R.string.spirometry, localeManager.getLanguage()),
            R.drawable.ic_icon_spirometry
        )

        val mHomeItem6 = HomeItem(
            7,
            getStringByLocalBefore17(context, R.string.fundoscopy, localeManager.getLanguage()),
            R.drawable.ic_icon_fundoscopy
        )

        val mHomeItem7 = HomeItem(
            8,
            getStringByLocalBefore17(context, R.string.activity_tracker, localeManager.getLanguage()),
            R.drawable.ic_icon_activity_tracker
        )

        val mHomeItem8 = HomeItem(
            9,
            getStringByLocalBefore17(context, R.string.screening_hlq, localeManager.getLanguage()),
            R.drawable.ic_icon_healthy_lifestyle
        )
        val mHomeItem9 = HomeItem(
            10,
            getStringByLocalBefore17(context, R.string.screening_intake24, localeManager.getLanguage()),
            R.drawable.ic_icon_intake
        )

        //  val mHomeItem7 = HomeItem(8, getStringByLocalBefore17(context, R.string.pathology, localeManager.getLanguage()), R.drawable.ic_icon_pathology)

        //   val mHomeItem8 = HomeItem(9, getStringByLocalBefore17(context, R.string.medical_report, localeManager.getLanguage()), R.drawable.ic_icon_medical_report)


        val test = ArrayList<HomeItem>()

        test.add(mHomeItem)
        test.add(mHomeItem1)
        test.add(mHomeItem2)

        test.add(mHomeItem4)
        test.add(mHomeItem5)
        test.add(mHomeItem6)

        test.add(mHomeItem3)
        test.add(mHomeItem7)
        test.add(mHomeItem8)
        test.add(mHomeItem9)
        //   test.add(mHomeItem7)


        val homeItems = MutableLiveData<Resource<List<HomeItem>>>()
        val resource = Resource(Status.SUCCESS, test, Message(null, null))
        homeItems.setValue(resource)

        return homeItems
    }


    fun getSampleItems(): LiveData<Resource<List<HomeItem>>> {


        val mHomeItem = HomeItem(
            1,
            getStringByLocalBefore17(context, R.string.sample_management_processing, localeManager.getLanguage()),
            R.drawable.ic_icon_pathology
        )

        val mHomeItem1 = HomeItem(
            2,
            getStringByLocalBefore17(context, R.string.sample_management_storage, localeManager.getLanguage()),
            R.drawable.ic_icon_cryo
        )


        val test = ArrayList<HomeItem>()

        test.add(mHomeItem)
        test.add(mHomeItem1)

        val homeItems = MutableLiveData<Resource<List<HomeItem>>>()
        val resource = Resource(Status.SUCCESS, test, Message(null, null))
        homeItems.setValue(resource)

        return homeItems

    }


    private fun getStringByLocalBefore17(context: Context, resId: Int, language: String): String {
        val currentResources = context.resources
        val assets = currentResources.assets
        val metrics = currentResources.displayMetrics
        val config = Configuration(currentResources.configuration)
        val locale = Locale(language)
        Locale.setDefault(locale)
        config.locale = locale
        val defaultLocaleResources = Resources(assets, metrics, config)
        val string = defaultLocaleResources.getString(resId)
        // Restore device-specific locale
        Resources(assets, metrics, currentResources.configuration)
        return string
    }
}
