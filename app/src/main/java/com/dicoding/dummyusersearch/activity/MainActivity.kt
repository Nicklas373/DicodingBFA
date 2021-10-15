package com.dicoding.dummyusersearch.activity

import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.SearchView
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.dicoding.dummyusersearch.R
import com.dicoding.dummyusersearch.adapter.GithubUserAdapter
import com.dicoding.dummyusersearch.databinding.ActivityMainBinding
import com.dicoding.dummyusersearch.datastore.SettingPreferences
import com.dicoding.dummyusersearch.userdata.GitHubUserArray
import com.dicoding.dummyusersearch.viewmodel.MainActivityViewModel
import com.dicoding.dummyusersearch.viewmodel.SettingPreferencesViewModel
import com.dicoding.dummyusersearch.viewmodel.ViewModelFactory

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class MainActivity : AppCompatActivity() {
    private val listGitHubUser = ArrayList<GitHubUserArray>()
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val pref = SettingPreferences.getInstance(dataStore)
        val mainViewModel = ViewModelProvider(this, ViewModelProvider.NewInstanceFactory()).get(
            MainActivityViewModel::class.java
        )
        val settingsViewModel = ViewModelProvider(this, ViewModelFactory(pref)).get(
            SettingPreferencesViewModel::class.java
        )
        val layoutManager = LinearLayoutManager(this)
        val itemDecoration = DividerItemDecoration(this, layoutManager.orientation)
        val searchManager = getSystemService(Context.SEARCH_SERVICE) as SearchManager
        val searchView = binding.gitSearch

        settingsViewModel.getThemeSettings().observe(this,
            { isDarkModeActive: Boolean ->
                if (isDarkModeActive) {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                } else {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                }
            })

        mainViewModel.githubUserArray.observe(this, { userArray ->
            setGitHubUserData(userArray)
        })

        mainViewModel.isLoading.observe(this, {
            showLoading(it)
        })

        mainViewModel.isToast.observe(this, { isToast ->
            showToast(isToast, mainViewModel.toastReason.value.toString())
        })

        binding.listGithubUser.layoutManager = layoutManager
        binding.listGithubUser.addItemDecoration(itemDecoration)

        title = resources.getString(R.string.app_name)

        searchView.setSearchableInfo(searchManager.getSearchableInfo(componentName))
        searchView.queryHint = resources.getString(R.string.git_search)
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                listGitHubUser.clear()
                mainViewModel.findGitHubUserID(query)
                val adapter = GithubUserAdapter(listGitHubUser)
                binding.listGithubUser.adapter = adapter
                return true
            }

            override fun onQueryTextChange(newText: String): Boolean {
                return false
            }
        })
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        setMode(item.itemId)
        return super.onOptionsItemSelected(item)
    }

    private fun setMode(selectedMode: Int) {
        when (selectedMode) {
            R.id.action_favourite -> {
                val intent = Intent(this, FavouriteActivity::class.java)
                startActivity(intent)
            }
            R.id.action_settings -> {
                val intent = Intent(this, SettingsActivity::class.java)
                startActivity(intent)
            }
        }
    }

    private fun setGitHubUserData(listGithubUserID: List<GitHubUserArray>) {
        val listReview = ArrayList<GitHubUserArray>()
        for (userID in listGithubUserID) {
            val user = GitHubUserArray(userID.login, userID.htmlUrl, userID.avatarUrl)
            listReview.add(user)
        }
        val adapter = GithubUserAdapter(listReview)
        binding.listGithubUser.adapter = adapter
    }

    private fun showLoading(isLoading: Boolean) {
        if (isLoading) {
            binding.progressBar.visibility = View.VISIBLE
        } else {
            binding.progressBar.visibility = View.GONE
        }
    }

    private fun showToast(isToast: Boolean, toastReason: String) {
        if (!isToast) {
            Toast.makeText(this, toastReason, Toast.LENGTH_LONG).show()
        }
    }
}