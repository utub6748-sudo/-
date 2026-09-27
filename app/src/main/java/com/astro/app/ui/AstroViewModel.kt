package com.astro.app.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.astro.app.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class AstroViewModel(private val repo: AstroRepository, private val auth: AuthRepository, private val appContext: Context) : ViewModel() {
    val transactions=repo.dao.transactions().stateIn(viewModelScope,SharingStarted.WhileSubscribed(5000),emptyList()); val habits=repo.dao.habits().stateIn(viewModelScope,SharingStarted.WhileSubscribed(5000),emptyList()); val goals=repo.dao.goals().stateIn(viewModelScope,SharingStarted.WhileSubscribed(5000),emptyList())
    val theme=repo.theme.stateIn(viewModelScope,SharingStarted.WhileSubscribed(5000),"system"); val accent=repo.accent.stateIn(viewModelScope,SharingStarted.WhileSubscribed(5000),0xFF8B5CF6); val nickname=repo.nickname.stateIn(viewModelScope,SharingStarted.WhileSubscribed(5000),"Astro User"); val baseCurrency=repo.baseCurrency.stateIn(viewModelScope,SharingStarted.WhileSubscribed(5000),"EUR"); val pinEnabled=repo.pinEnabled.stateIn(viewModelScope,SharingStarted.WhileSubscribed(5000),false)
    val loggedIn=MutableStateFlow(auth.isLoggedIn()); val authMessage=MutableStateFlow<String?>(null); val fx=MutableStateFlow<FxResponse?>(null); val aiAnswer=MutableStateFlow<String?>(null); val aiBusy=MutableStateFlow(false); val fxBusy=MutableStateFlow(false)
    init{viewModelScope.launch{repo.seed()};refreshFx()}
    fun addTransaction(title:String,amount:Double,currency:String,category:String,income:Boolean)=viewModelScope.launch{repo.dao.insertTransaction(TransactionEntity(title=title,amount=amount,currency=currency.uppercase(),category=category,type=if(income)"income" else "expense"))}
    fun addHabit(title:String)=viewModelScope.launch{repo.dao.insertHabit(HabitEntity(title=title))}; fun toggleHabit(h:HabitEntity)=viewModelScope.launch{repo.dao.updateHabit(h.copy(completedToday=!h.completedToday,streak=if(!h.completedToday)h.streak+1 else(h.streak-1).coerceAtLeast(0)))}
    fun addGoal(title:String,target:Double?,currency:String)=viewModelScope.launch{repo.dao.insertGoal(GoalEntity(title=title,targetAmount=target,currency=currency.uppercase()))}; fun toggleGoal(g:GoalEntity)=viewModelScope.launch{repo.dao.updateGoal(g.copy(completed=!g.completed))}
    fun setTheme(v:String)=viewModelScope.launch{repo.setTheme(v)}; fun setAccent(v:Long)=viewModelScope.launch{repo.setAccent(v)}; fun setNickname(v:String)=viewModelScope.launch{repo.setNickname(v)}; fun setBaseCurrency(v:String)=viewModelScope.launch{repo.setBaseCurrency(v);refreshFx(v)}; fun setPin(v:String)=viewModelScope.launch{repo.setPin(v)}; fun clearPin()=viewModelScope.launch{repo.clearPin()}; suspend fun verifyPin(pin:String)=repo.verifyPin(pin)
    fun signup(email:String,password:String)=viewModelScope.launch{authMessage.value=auth.signUp(email,password).fold({it},{it.message?:"Ошибка регистрации"})}; fun verify(email:String,token:String)=viewModelScope.launch{authMessage.value=auth.verifyEmail(email,token).fold({loggedIn.value=true;it},{it.message?:"Неверный код"})}; fun login(email:String,password:String)=viewModelScope.launch{authMessage.value=auth.login(email,password).fold({loggedIn.value=true;it},{it.message?:"Ошибка входа"})}; fun logout(){auth.logout();loggedIn.value=false}
    fun refreshFx(base:String=baseCurrency.value)=viewModelScope.launch{fxBusy.value=true;fx.value=runCatching{FxService().latest(base)}.getOrNull();fxBusy.value=false}; fun askAi(q:String)=viewModelScope.launch{aiBusy.value=true;val ctx="Транзакции: ${transactions.value.take(20).joinToString{it.title+":"+it.amount+it.currency}}; привычки: ${habits.value.joinToString{it.title+":"+it.completedToday}}; цели: ${goals.value.joinToString{it.title+":"+it.completed}}";aiAnswer.value=AiService(appContext).ask("$q\n\nКонтекст пользователя: $ctx").fold({it},{"Не удалось получить ответ: ${it.message}"});aiBusy.value=false}
    class Factory(private val context:Context):ViewModelProvider.Factory{override fun <T:ViewModel> create(modelClass:Class<T>):T=AstroViewModel(AstroRepository(context),AuthRepository(context),context) as T}
}
