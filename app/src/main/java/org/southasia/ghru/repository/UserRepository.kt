package org.southasia.ghru.repository

import androidx.lifecycle.LiveData
import org.southasia.ghru.AppExecutors
import org.southasia.ghru.api.NghruService
import org.southasia.ghru.db.UserDao
import org.southasia.ghru.util.TokenManager
import org.southasia.ghru.vo.Resource
import org.southasia.ghru.vo.ResourceData
import org.southasia.ghru.vo.User
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository that handles User objects.
 */

@Singleton
class UserRepository @Inject constructor(
    private val appExecutors: AppExecutors,
    private val userDao: UserDao,
    private val nghruService: NghruService,
    private val tokenManager: TokenManager

) {

    fun loadUser(): LiveData<Resource<User>> {
        return object : NetworkBoundResource<User, ResourceData<User>>(appExecutors) {
            override fun saveCallResult(item: ResourceData<User>) {
                print("User " + item.data)
                userDao.insert(item.data!!)
            }

            override fun shouldFetch(data: User?): Boolean = data == null

            override fun loadFromDb() = userDao.findByLogin(tokenManager.getEmail()!!)

            override fun createCall() = nghruService.getUser()
        }.asLiveData()
    }

    fun loadUserDB(): LiveData<Resource<User>> {
        return object : NetworkBoundResource<User, ResourceData<User>>(appExecutors) {


            override fun saveCallResult(item: ResourceData<User>) {
                userDao.insert(item.data!!)
            }

            override fun shouldFetch(data: User?): Boolean = data == null

            override fun loadFromDb() = userDao.findByLogin(tokenManager.getEmail()!!)

            override fun createCall() = nghruService.getUser()
        }.asLiveData()
    }

}
