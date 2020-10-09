/*
 * Copyright (c) 2020 Razeware LLC
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * Notwithstanding the foregoing, you may not use, copy, modify, merge, publish,
 * distribute, sublicense, create a derivative work, and/or sell copies of the
 * Software in any work that is designed, intended, or marketed for pedagogical or
 * instructional purposes related to programming, coding, application development,
 * or information technology.  Permission for such use, copying, modification,
 * merger, publication, distribution, sublicensing, creation of derivative works,
 * or sale is expressly withheld.
 *
 * This project and source code may use libraries or frameworks that are
 * released under various Open-Source licenses. Use of those libraries and
 * frameworks are governed by their own individual licenses.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.raywenderlich.android.redditclone.repositories

import android.content.Context
import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.raywenderlich.android.redditclone.database.RedditDatabase
import com.raywenderlich.android.redditclone.models.RedditPost
import com.raywenderlich.android.redditclone.networking.RedditClient
import com.raywenderlich.android.redditclone.networking.RedditService
import kotlinx.coroutines.flow.Flow

class RedditRepo(context: Context) {

    // 1 create a reference to RedditService to download list of posts from the Reddit API
    private val redditService = RedditClient.getClient().create(RedditService::class.java)

    //creates an instance of the Room database using the create(). You’ll use it with RemoteMediator.
    private val redditDatabase = RedditDatabase.create(context)

    /*Note: RemoteMediator API is currently experimental
    and needs to be marked as OptIn via the @OptIn(ExperimentalPagingApi::class) annotation in the classes using it.*/
    @OptIn(ExperimentalPagingApi::class)
    fun fetchPosts(): Flow<PagingData<RedditPost>> {
        return Pager(
            PagingConfig(
                pageSize = 40,
                enablePlaceholders = true,
                // 1 As part of your paging configuration, you add prefetchDistance to PagingConfig.
                // This parameter defines when to trigger the load of the next items within the loaded list.
                prefetchDistance = 3),

            // 2  set RedditRemoteMediator.RedditRemoteMediator fetches the data from the network and saves it to the database.
            remoteMediator = RedditRemoteMediator(redditService, redditDatabase),

            // 3 set pagingSourceFactory.all the Dao to get your posts.
            // Now your database serves as a single source of truth for the posts you display, whether or not you have a network connection.
            pagingSourceFactory = { redditDatabase.redditPostsDao().getPosts() }
        ).flow
    }



}

/*You don’t have to modify the ViewModel or the activity layer,
since nothing has changed there! That’s the benefit of choosing a good architecture.
You can swap the data source implementations without modifying other layers in your app.*/