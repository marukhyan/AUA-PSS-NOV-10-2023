package com.example.todolistapi

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val viewModel = ViewModelProvider(this)[ToDoViewModel::class.java]

        setContent {
            AppUI(viewModel = viewModel)
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun AppUI(viewModel: ToDoViewModel) {
        viewModel.todos.observe(this) {
            setContent {
                Column {
                    TopAppBar(
                        title = { Text(text = "Todo List") }
                    )

                    if (it.isNullOrEmpty()) {
                        Text(text = "Loading todos...")
                    } else {
                        ToDoList(todos = it)
                    }
                }
            }
        }
    }
}

class ToDoViewModel : ViewModel() {
    private val todoService = RetrofitClient.todoService

    private val _todos = MutableLiveData<List<Todo>>()
    val todos: LiveData<List<Todo>> get() = _todos

    init {
        viewModelScope.launch {
            try {
                val response = todoService.getToDos()
                _todos.value = response
            } catch (e: Exception) {
                Log.e("Retrofit", "Error fetching user data", e)
            }
        }
    }
}

@Composable
fun ToDoList(todos: List<Todo>) {
    LazyColumn {
        items(todos) { todo ->
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = todo.title,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

object RetrofitClient {
    private const val BASE_URL = "https://jsonplaceholder.typicode.com/"

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val todoService: ToDoService by lazy {
        retrofit.create(ToDoService::class.java)
    }
}

data class Todo(
    val userId: Int,
    val id: Int,
    val title: String,
    val completed: Boolean
)

interface ToDoService {
    @GET("todos")
    suspend fun getToDos(): List<Todo>
}