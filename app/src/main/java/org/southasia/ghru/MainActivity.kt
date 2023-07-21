package org.southasia.ghru

import android.app.job.JobInfo
import android.app.job.JobScheduler
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.*
import androidx.navigation.Navigation
import androidx.navigation.ui.setupWithNavController
import com.birbit.android.jobqueue.JobManager
import com.crashlytics.android.Crashlytics
import com.google.android.material.navigation.NavigationView
import dagger.android.DispatchingAndroidInjector
import dagger.android.support.HasSupportFragmentInjector
import io.fabric.sdk.android.Fabric
import org.southasia.ghru.databinding.MainActivityBinding
import org.southasia.ghru.db.*
import org.southasia.ghru.network.NetworkSchedulerService
import org.southasia.ghru.repository.*
import org.southasia.ghru.sync.*
import org.southasia.ghru.util.AbsentLiveData
import org.southasia.ghru.util.LocaleManager
import org.southasia.ghru.vo.*
import org.southasia.ghru.vo.request.*
import org.southasia.ghru.vo.request.Member
import timber.log.Timber
import java.net.URL
import javax.inject.Inject
import kotlin.concurrent.thread


class MainActivity : AppCompatActivity(), HasSupportFragmentInjector {
    @Inject
    lateinit var dispatchingAndroidInjector: DispatchingAndroidInjector<Fragment>

    override fun supportFragmentInjector() = dispatchingAndroidInjector

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var drawerToggle: ActionBarDrawerToggle

    @Inject
    lateinit var localeManager: LocaleManager

    private val registry = LifecycleRegistry(this)

    @Inject
    lateinit var syncCommentLifecycleObserver: SyncHouseholdLifecycleObserver

    @Inject
    lateinit var syncHouseholdMemberLifecycleObserver: SyncHouseholdMemberLifecycleObserver

    @Inject
    lateinit var memberDao : MemberDao

    @Inject
    lateinit var syncParticipantRequestLifecycleObserver: SyncParticipantRequestLifecycleObserver

    @Inject
    lateinit var participantRequestDao : ParticipantRequestDao

    @Inject
    lateinit var bodyMeasurementRequestDao: BodyMeasurementRequestDao

    @Inject
    lateinit var sampleRequestDao: SampleRequestDao

    @Inject
    lateinit var sampleProcessDao: SampleProcessDao

    @Inject
    lateinit var sampleStorageRequestDao: SampleStorageRequestDao

    lateinit var bodyMeasurementRequestLifecycleObserver: BodyMeasurementRequestLifecycleObserver


    @Inject
    lateinit var bloodPressureRequestDao: BloodPresureRequestDao

    lateinit var bloodPressureMetaRequestLifecycleObserver: BloodPressureMetaRequestLifecycleObserver

    lateinit var syncSampleRequestLifecycleObserver: SyncSampleRequestLifecycleObserver

    lateinit var syncSampleProcessLifecycleObserver: SyncSampleProcessLifecycleObserver


    lateinit var syncSampleStorageRequestLifecycleObserver: SyncSampleStorageRequestLifecycleObserver

    lateinit var syncHouseholdRequestmetaLifecycleObserver: SyncHouseholdRequestmetaLifecycleObserver

    lateinit var bodyMeasurementMetaLifecycleObserver: BodyMeasurementMetaLifecycleObserver

    lateinit var spirometryRequestRxBusLifecycleObserver: SpirometryRequestRxBusLifecycleObserver



    @Inject
    lateinit var householdRequestMetaMetaDao: HouseholdRequestMetaMetaDao


    @Inject
    lateinit var bodyMeasurementMetaDao: BodyMeasurementMetaDao


    @Inject
    lateinit var spiromentryRequestDao: SpiromentryRequestDao

    @Inject
    lateinit var userRepository: UserRepository
    private val _email = MutableLiveData<String>()


    @Inject
    lateinit var questionnaireRepository: QuestionnaireRepository

    @Inject
    lateinit var sampleRequestRepository: SampleRequestRepository

    @Inject
    lateinit var householdRequestRepository: HouseholdRequestRepository

    @Inject
    lateinit var membersRepository: MembersRepository

    private val _language = MutableLiveData<String>()


    var mainActivityBinding: MainActivityBinding? = null

    @Inject
    lateinit var jobManager: JobManager

    val LANGUAGE_ENGLISH = "en"
    val LANGUAGE_URDU = "ur"
    val LANGUAGE_HINDI = "hi"
    val LANGUAGE_BENGALI = "bn"

    @Inject
    lateinit var ecgStatusDao: ECGStatusDao
    lateinit var ecgStatusLifecycleObserver: ECGStatusLifecycleObserver

    @Inject
    lateinit var cancelRequestDao : CancelRequestDao
    lateinit var cancelRequestLifecycleObserver: CancelRequestLifecycleObserver

    @Inject
    lateinit var fundoscopyRequestDao: FundoscopyRequestDao
    lateinit var fundoscopyRequestLifecycleObserver: FundoscopyRequestLifecycleObserver

    @Inject
    lateinit var axivityDao: AxivityDao
    lateinit var axivityRequestLifecycleObserver: AxivityRequestLifecycleObserver

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //setContentView(R.layout.main_activity)
//        Crashlytics.setString("participantRequest", "sss")
//        Crashlytics.setString("participantMeta", "staringggss")
//        Crashlytics.logException(Exception("shanuka tes" ))
        mainActivityBinding = DataBindingUtil.setContentView<MainActivityBinding>(this, R.layout.main_activity)
        lifecycle.addObserver(syncCommentLifecycleObserver);

        syncHouseholdMemberLifecycleObserver = SyncHouseholdMemberLifecycleObserver(memberDao)
        lifecycle.addObserver(syncHouseholdMemberLifecycleObserver)

        syncParticipantRequestLifecycleObserver = SyncParticipantRequestLifecycleObserver(participantRequestDao)
        lifecycle.addObserver(syncParticipantRequestLifecycleObserver)

        bodyMeasurementRequestLifecycleObserver = BodyMeasurementRequestLifecycleObserver(bodyMeasurementRequestDao)
        syncSampleRequestLifecycleObserver = SyncSampleRequestLifecycleObserver(sampleRequestDao)
        lifecycle.addObserver(bodyMeasurementRequestLifecycleObserver)

        lifecycle.addObserver(syncSampleRequestLifecycleObserver)
        syncSampleProcessLifecycleObserver = SyncSampleProcessLifecycleObserver(sampleProcessDao)
        lifecycle.addObserver(syncSampleProcessLifecycleObserver)
        syncSampleStorageRequestLifecycleObserver = SyncSampleStorageRequestLifecycleObserver(sampleStorageRequestDao)
        lifecycle.addObserver(syncSampleStorageRequestLifecycleObserver)

        syncHouseholdRequestmetaLifecycleObserver = SyncHouseholdRequestmetaLifecycleObserver(householdRequestMetaMetaDao)
        lifecycle.addObserver(syncHouseholdRequestmetaLifecycleObserver)

        bloodPressureMetaRequestLifecycleObserver = BloodPressureMetaRequestLifecycleObserver(bloodPressureRequestDao)
        lifecycle.addObserver(bloodPressureMetaRequestLifecycleObserver)

        bodyMeasurementMetaLifecycleObserver = BodyMeasurementMetaLifecycleObserver(bodyMeasurementMetaDao)
        lifecycle.addObserver(bodyMeasurementMetaLifecycleObserver)


        ecgStatusLifecycleObserver = ECGStatusLifecycleObserver(ecgStatusDao)
        lifecycle.addObserver(ecgStatusLifecycleObserver)

        cancelRequestLifecycleObserver = CancelRequestLifecycleObserver(cancelRequestDao)
        lifecycle.addObserver(cancelRequestLifecycleObserver)


        spirometryRequestRxBusLifecycleObserver = SpirometryRequestRxBusLifecycleObserver(spiromentryRequestDao)
        lifecycle.addObserver(spirometryRequestRxBusLifecycleObserver)

        fundoscopyRequestLifecycleObserver = FundoscopyRequestLifecycleObserver(fundoscopyRequestDao)
        lifecycle.addObserver(fundoscopyRequestLifecycleObserver)

        axivityRequestLifecycleObserver = AxivityRequestLifecycleObserver(axivityDao)
        lifecycle.addObserver(axivityRequestLifecycleObserver)

        // //L.d("Simpe log");
        Fabric.with(this,  Crashlytics());
       // Crashlytics.getInstance().crash() // Force a crash

        scheduleJob();
        showDb()
        if (isNetworkAvailable()) {
            thread() {
                var online = true

                try {
                    val myUrl = URL(BuildConfig.SERVER_URL)
                    val connection = myUrl.openConnection()
                    connection.setConnectTimeout(5000)
                    connection.connect()
                    online = true
                } catch (e: Exception) {
                    // Handle your exceptions
                    online = false
                }
                if (online) {
                    //jobManager.count()
                    jobManager.start()
                } else {
                    jobManager.stop()
                }
            }


        }
        setupToolbar()
        drawerLayout = findViewById(R.id.drawer_layout)

//        drawerToggle = ActionBarDrawerToggle(
//                this, drawerLayout, R.string.app_name, R.string.app_name)

        drawerToggle = object : ActionBarDrawerToggle(
            this,
            drawerLayout,

            R.string.app_name,
            R.string.app_name
        ) {
            override fun onDrawerClosed(view: View) {
                invalidateOptionsMenu()
            }

            override fun onDrawerOpened(drawerView: View) {
                invalidateOptionsMenu()
                _email.value = "MainActivity"
            }

        }
        drawerLayout.addDrawerListener(drawerToggle)

        val user: LiveData<Resource<User>>? = Transformations
            .switchMap(_email) { emailx ->
                if (emailx == null) {
                    AbsentLiveData.create()
                } else {
                    userRepository.loadUserDB()
                }
            }

        user?.observe(this, Observer { userData ->
            if (userData?.data != null) {
                setupNavigationDrawer(userData.data)
            }

        })


        _language.value = LANGUAGE_ENGLISH
        setSampleIdAll(LANGUAGE_ENGLISH)
        setId(LANGUAGE_ENGLISH)
        val questionnaire: LiveData<Resource<List<Questionnaire>>>? = Transformations
            .switchMap(_language) { language ->
                if (language == null) {
                    AbsentLiveData.create()
                } else {
                    questionnaireRepository.getQuestionnaireList(network = isNetworkAvailable(), language =  language)
                }
            }

        questionnaire?.observe(this, Observer { userData ->
            if (userData?.data != null) {
               // setupNavigationDrawer(userData.data)
            }

        })

        // sample sync


        var screeningIdCheckAll: LiveData<Resource<List<SampleRequest>>>? = Transformations
            .switchMap(_sampleIdAll) { sampleId ->
                if (sampleId == null) {
                    AbsentLiveData.create()
                } else {
                    sampleRequestRepository.getSamples()
                }
            }

        screeningIdCheckAll?.observe(this, Observer { userData ->
            if (userData?.data != null) {
                // setupNavigationDrawer(userData.data)
            }

        })

        val MembersSave: LiveData<Resource<List<Member>>>? = Transformations
            .switchMap(_member) { search ->
                if (search == null) {
                    AbsentLiveData.create()
                } else {
                    membersRepository.insertMembersSave(search)
                }
            }

        val householdRequestMetasSave: LiveData<Resource<List<HouseholdRequestMeta>>>? = Transformations
            .switchMap(_householdRequestMetas) { search ->
                if (search == null) {
                    AbsentLiveData.create()
                } else {
                    householdRequestRepository.insertHouseholdRequestAll(search)
                }
            }

        val visitedHouseholdItem: LiveData<Resource<ResourceData<List<HouseholdRequestMetaResponce>>>>? = Transformations
            .switchMap(_visitedHousehold) { login ->
                if (login == null) {
                    AbsentLiveData.create()
                } else {
                    householdRequestRepository.getHouseHolds();
                }
            }


        visitedHouseholdItem?.observe(this, Observer { resource ->

            if (resource?.status == Status.SUCCESS) {

                val list: ArrayList<HouseholdRequest> = ArrayList<HouseholdRequest>()
                val householdRequestMetaList: ArrayList<HouseholdRequestMeta> = ArrayList<HouseholdRequestMeta>()
                val memberList: ArrayList<Member> = ArrayList<Member>()
                resource.data?.data?.forEach { household ->
                    val householdRequestMeta = HouseholdRequestMeta(
                        meta = household.meta,
                        uuid = household.uuid!!,
                        householdRequest = household.householdRequest
                    )

                    val element = household.householdRequest!!
                    //element.syncPending = false
                    list.add(element)
                    household.memberList?.forEach { member ->
                        member.householdId = household.householdRequest.enumerationId
                        member.registed = member.studyStatus?.registered
                    }
                    memberList.addAll(household.memberList!!)
                    householdRequestMetaList.add(householdRequestMeta)
                }
                setHouseholdRequestMetas(householdRequestMetaList)
                setMembers(memberList)

            } else {
                // adapter.submitList(emptyList())
            }
        })


        MembersSave?.observe(this, Observer { resource ->

            if (resource?.data != null) {
                //L.d("data saved")
            }

        })

    }

    fun setId(lang: String?) {
        if (_visitedHousehold.value != lang) {
            _visitedHousehold.value = lang
        }
    }


    private val _visitedHousehold = MutableLiveData<String>()

    private val _householdRequestMetas = MutableLiveData<List<HouseholdRequestMeta>>()


    fun setHouseholdRequestMetas(householdRequestMetas: List<HouseholdRequestMeta>?) {
        if (_householdRequestMetas.value != householdRequestMetas) {
            _householdRequestMetas.value = householdRequestMetas
        }
    }


    private val _member = MutableLiveData<List<Member>>()



    fun setMembers(members: List<Member>?) {
        if (_member.value != members) {
            _member.value = members
        }
    }
    private val _sampleIdAll: MutableLiveData<String> = MutableLiveData()

    fun setSampleIdAll(sampleId: String?) {
        _sampleIdAll.value = sampleId
    }

    private fun setupToolbar() {
        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.run {
            setDisplayHomeAsUpEnabled(true)
            setHomeButtonEnabled(true)
            setDisplayShowTitleEnabled(false);

        }
    }

    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = this.getSystemService(Context.CONNECTIVITY_SERVICE)
        return if (connectivityManager is ConnectivityManager) {
            val networkInfo: NetworkInfo? = connectivityManager.activeNetworkInfo
            networkInfo?.isConnected ?: false
        } else false
    }


    private fun setupNavigationDrawer(data: User) {
        //Crashlytics.setUserIdentifier(data.toString());
        val navController = Navigation.findNavController(this, R.id.nghru_nav_fragment)
        val nav_view: NavigationView = findViewById(R.id.navigation_view)
        val hView = nav_view.getHeaderView(0)
        val textViewName = hView.findViewById(R.id.textViewName) as TextView
        val textViewEmail = hView.findViewById(R.id.textViewEmail) as TextView
        textViewName.setText(data.name)
        textViewEmail.setText(data.email)
        nav_view.setupWithNavController(navController)


//        nav_view.setNavigationItemSelectedListener { it ->
//            drawerLayout.closeDrawer(GravityCompat.START)
//            when (it.itemId) {
//                R.id.logoutDialogFragment -> {
//                    val logoutDialogFragment = LogoutDialogFragment()
//                    logoutDialogFragment.show(supportFragmentManager!!)
//                }
//            }
//            true
//        }

    }


    private fun scheduleJob() {
        val myJob = JobInfo.Builder(0, ComponentName(this, NetworkSchedulerService::class.java))
            .setRequiresCharging(true)
            .setMinimumLatency(1000)
            .setOverrideDeadline(2000)
            .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
            .setPersisted(true)
            .build()
        val jobScheduler = getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler
        jobScheduler.schedule(myJob)
    }


    override fun onStop() {
        super.onStop()
        stopService(Intent(this, NetworkSchedulerService::class.java))
    }

    override fun onStart() {
        super.onStart()
        // Start service and provide it a way to communicate with this class.
        val startServiceIntent = Intent(this, NetworkSchedulerService::class.java)
        startService(startServiceIntent)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // The action bar home/up action should open or close the drawer.
        // [ActionBarDrawerToggle] will take care of this.
        if (drawerToggle.onOptionsItemSelected(item)) {
            return true
        }
        item.setChecked(true);

        when (item.itemId) {
            R.id.menu_english -> {
                setNewLocale(LANGUAGE_ENGLISH, false)
                return true
            }
            R.id.menu_bengali -> {
                setNewLocale(LANGUAGE_BENGALI, false)
                return true
            }
            R.id.menu_hindi -> {
                setNewLocale(LANGUAGE_HINDI, false)
                return true
            }
            R.id.menu_urdu -> {
                setNewLocale(LANGUAGE_URDU, false)
                return true
            }
        }

        return super.onOptionsItemSelected(item)
    }

    /**
     * If [ActionBarDrawerToggle] is used, it must be called in [onPostCreate] and
     * [onConfigurationChanged].
     */
    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        // Sync the toggle state after has occurred.
        drawerToggle.syncState()
    }

    override fun onConfigurationChanged(newConfig: Configuration?) {
        super.onConfigurationChanged(newConfig)
        // Pass any configuration change to the drawer toggle.
        drawerToggle.onConfigurationChanged(newConfig)
    }

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(LocaleManager(base).setLocale())
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu to use in the action bar
        val inflater = menuInflater
        inflater.inflate(R.menu.menu_language, menu)

        return super.onCreateOptionsMenu(menu)
    }

    private fun setNewLocale(language: String, restartProcess: Boolean): Boolean {
        localeManager.setNewLocale(language)
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK))
        if (restartProcess) {
            System.exit(0)
        }
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {

        when (localeManager.getLanguage()) {
            LANGUAGE_ENGLISH -> menu?.findItem(R.id.menu_english)?.isChecked = true
            LANGUAGE_URDU -> menu?.findItem(R.id.menu_urdu)?.isChecked = true
            LANGUAGE_HINDI -> menu?.findItem(R.id.menu_hindi)?.isChecked = true
            LANGUAGE_BENGALI -> menu?.findItem(R.id.menu_bengali)?.isChecked = true

            else -> { // Note the block
                setNewLocale(LANGUAGE_ENGLISH, false)
            }
        }
        return super.onPrepareOptionsMenu(menu)
    }

    fun showDb(){
        if (BuildConfig.DEBUG) {
            try {
                val debugDB = Class.forName("com.amitshekhar.DebugDB")
                val getAddressLog = debugDB.getMethod("getAddressLog")
                val value = getAddressLog.invoke(null)
                Timber.e(value.toString())
            } catch ( ignore: Exception) {

            }
        }
    }

}
