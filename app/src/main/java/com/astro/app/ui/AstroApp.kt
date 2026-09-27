package com.astro.app.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Alignment
import com.astro.app.data.*

@Composable
fun AstroApp(vm: AstroViewModel) {
    var tab by remember { mutableIntStateOf(0) }
    var add by remember { mutableStateOf(false) }
    Scaffold(bottomBar={NavigationBar{listOf("Главная","Финансы","Привычки","Цели").forEachIndexed{ i,t->NavigationBarItem(selected=tab==i,onClick={tab=i},icon={},label={Text(t)})}}}){ pad ->
        when(tab){
            0 -> Home(vm,Modifier.padding(pad))
            1 -> Finance(vm,Modifier.padding(pad),{add=true})
            2 -> Habits(vm,Modifier.padding(pad))
            else -> Goals(vm,Modifier.padding(pad))
        }
    }
    if(add) AddTransaction(vm){add=false}
}

@Composable private fun Home(vm:AstroViewModel,m:Modifier){val tx by vm.transactions.collectAsState();val habits by vm.habits.collectAsState();val goals by vm.goals.collectAsState();val income=tx.filter{it.type=="income"}.sumOf{it.amount};val expense=tx.filter{it.type=="expense"}.sumOf{it.amount};LazyColumn(m.fillMaxSize().padding(18.dp),verticalArrangement=Arrangement.spacedBy(12.dp)){item{Text("Astro",style=MaterialTheme.typography.headlineLarge);Text("Личный финансовый и жизненный dashboard",color=MaterialTheme.colorScheme.onSurfaceVariant)};item{Card(Modifier.fillMaxWidth()){Column(Modifier.padding(18.dp)){Text("Баланс",style=MaterialTheme.typography.titleMedium);Text(String.format("%.2f €",income-expense),style=MaterialTheme.typography.headlineMedium);Text("Доходы %.2f  •  Расходы %.2f".format(income,expense),color=MaterialTheme.colorScheme.onSurfaceVariant)}}};item{Text("Сегодня",style=MaterialTheme.typography.titleLarge)};item{Text("Привычек: ${habits.size}   •   Целей: ${goals.size}")};items(tx.take(5)){Text("${it.title}: ${it.amount} ${it.currency} • ${it.category}")}}}

@Composable private fun Finance(vm:AstroViewModel,m:Modifier,onAdd:()->Unit){val tx by vm.transactions.collectAsState();Column(m.fillMaxSize().padding(18.dp)){Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween,verticalAlignment=Alignment.CenterVertically){Text("Финансы",style=MaterialTheme.typography.headlineMedium);Button(onClick=onAdd){Text("+ Операция")}};Spacer(Modifier.height(12.dp));LazyColumn(verticalArrangement=Arrangement.spacedBy(8.dp)){items(tx){t->Card(Modifier.fillMaxWidth()){Row(Modifier.padding(14.dp),horizontalArrangement=Arrangement.SpaceBetween){Column(Modifier.weight(1f)){Text(t.title);Text("${t.category} • ${t.currency}",color=MaterialTheme.colorScheme.onSurfaceVariant)};Text((if(t.type=="income")"+" else "-")+" ${t.amount}")}}}}}}

@Composable private fun Habits(vm:AstroViewModel,m:Modifier){val habits by vm.habits.collectAsState();var title by remember{mutableStateOf("")};Column(m.fillMaxSize().padding(18.dp)){Text("Привычки",style=MaterialTheme.typography.headlineMedium);Row(verticalAlignment=Alignment.CenterVertically){OutlinedTextField(title,{title=it},label={Text("Новая привычка")},modifier=Modifier.weight(1f));Button(onClick={if(title.isNotBlank()){vm.addHabit(title);title=""}},modifier=Modifier.padding(start=8.dp)){Text("+")}};LazyColumn{items(habits){h->ListItem(headlineContent={Text(h.title)},supportingContent={Text("Серия: ${h.streak}")},trailingContent={Checkbox(h.completedToday,{vm.toggleHabit(h)})})}}}}

@Composable private fun Goals(vm:AstroViewModel,m:Modifier){val goals by vm.goals.collectAsState();var title by remember{mutableStateOf("")};Column(m.fillMaxSize().padding(18.dp)){Text("Цели",style=MaterialTheme.typography.headlineMedium);Row(verticalAlignment=Alignment.CenterVertically){OutlinedTextField(title,{title=it},label={Text("Новая цель")},modifier=Modifier.weight(1f));Button(onClick={if(title.isNotBlank()){vm.addGoal(title,null,"EUR");title=""}},modifier=Modifier.padding(start=8.dp)){Text("+")}};LazyColumn{items(goals){g->ListItem(headlineContent={Text(g.title)},supportingContent={Text(g.targetAmount?.toString()?.plus(" ${g.currency}")?:"Без суммы")},trailingContent={Checkbox(g.completed,{vm.toggleGoal(g)})})}}}}

@Composable private fun AddTransaction(vm:AstroViewModel,close:()->Unit){var title by remember{mutableStateOf("")};var amount by remember{mutableStateOf("")};AlertDialog(onDismissRequest=close,title={Text("Новая операция")},text={Column{OutlinedTextField(title,{title=it},label={Text("Название")},singleLine=true);OutlinedTextField(amount,{amount=it},label={Text("Сумма")},singleLine=true,modifier=Modifier.padding(top=8.dp))}},confirmButton={Button(onClick={amount.toDoubleOrNull()?.let{vm.addTransaction(title.ifBlank{"Операция"},it,"EUR","Другое",false);close()}}){Text("Сохранить")}},dismissButton={TextButton(onClick=close){Text("Отмена")}})}
