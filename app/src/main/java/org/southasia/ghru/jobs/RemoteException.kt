package org.southasia.ghru.jobs

import retrofit2.Response

class RemoteException(val response: Response<*>) : Exception()
