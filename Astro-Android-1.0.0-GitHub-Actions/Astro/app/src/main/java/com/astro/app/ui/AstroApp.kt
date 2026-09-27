package com.astro.app.ui

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import com.astro.app.data.*
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.abs

private val SurfaceShape = RoundedCornerShape(24.dp)

@Composable
fun AstroApp(vm: AstroViewModel) {
    var tab by rememberSaveable { mutableIntStateOf(0) }
    var showAdd by remember { mutableStateOf(false) }
    var showAccount by remember { mutableStateOf(false) }
    val tx by vm.transactions.collectAsState()
    val habits by vm.habits.collectAsState()
    val goals by vm.goals.collectAsState()
    val nickname by vm.nickname.collectAsState()
    val currency by vm.baseCurrency.collectAsState()
    val accent by vm.accent.collectAsState()
    val fx by vm.fx.collectAsState()
    val pinEnabled by vm.pinEnabled.collectAsState()
    var unlocked by rememberSaveable { mutableStateOf(false) }

    if (pinEnabled && !unlocked) { PinLock(vm, onUnlock = { unlocked = true }); return }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = { NavigationBar { navItems.forEachIndexed { i, item -> NavigationBarItem(selected = tab == i, onClick = { tab = i }, icon = { Icon(item.second, null) }, label = { Text(item.first) }) } } },
        floatingActionButton = {
            if (tab in 1..3) FloatingActionButton(onClick = { showAdd = true }, containerColor = MaterialTheme.colorScheme.primary) { Icon(Icons.Default.Add, "Добавить") }
        }
    ) { pad ->
        Box(Modifier.fillMaxSize().padding(pad)) {
            when (tab) {
                0 -> Dashboard(tx, habits, goals, currency, fx, nickname, onAccount = { showAccount = true }, onAi = { tab = 4 })
                1 -> Finances(tx, currency)
                2 -> Habits(habits, vm)
                3 -> Goals(goals, vm)
                4 -> AiScreen(vm)
                5 -> Settings(vm, accent)
            }
        }
    }
    if (showAdd) {
        when (tab) {
            1 -> AddTransactionDialog(vm) { showAdd = false }
            2 -> AddHabitDialog(vm) { showAdd = false }
            3 -> AddGoalDialog(vm) { showAdd = false }
            else -> {}
        }
    }
    if (showAccount) AccountDialog(vm) { showAccount = false }
}

private val navItems = listOf(
    "Обзор" to Icons.Default.Home,
    "Финансы" to Icons.Default.AccountBalanceWallet,
    "Привычки" to Icons.Default.CheckCircle,
    "Цели" to Icons.Default.Flag,
    "ИИ" to Icons.Default.AutoAwesome,
    "Настройки" to Icons.Default.Settings
)

@Composable
private fun Header(title: String, subtitle: String? = null, action: (@Composable () -> Unit)? = null) {
    Row(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 18.dp), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) { Text(title, fontSize = 30.sp, fontWeight = FontWeight.Bold); subtitle?.let { Text(it, color = MaterialTheme.colorScheme.onSurfaceVariant) } }
        action?.invoke()
    }
}

@Composable
private fun Dashboard(tx: List<TransactionEntity>, habits: List<HabitEntity>, goals: List<GoalEntity>, currency: String, fx: FxResponse?, nickname: String, onAccount: () -> Unit, onAi: () -> Unit) {
    val balance = tx.sumOf { if (it.type == "income" && it.currency == currency) it.amount else if (it.type == "expense" && it.currency == currency) -it.amount else 0.0 }
    val income = tx.filter { it.type == "income" && it.currency == currency }.sumOf { it.amount }
    val expense = tx.filter { it.type == "expense" && it.currency == currency }.sumOf { it.amount }
    LazyColumn(contentPadding = PaddingValues(bottom = 24.dp)) {
        item { Header("Astro", "Добрый день, $nickname", action = { IconButton(onClick = onAccount) { Icon(Icons.Default.Person, null) } }) }
        item {
            Card(Modifier.padding(horizontal = 16.dp).fillMaxWidth(), shape = SurfaceShape, colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                Column(Modifier.padding(22.dp)) {
                    Text("Общий баланс", color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = .75f))
                    Text(formatMoney(balance, currency), fontSize = 34.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 6.dp))
                    Row(Modifier.padding(top = 18.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Metric("Доход", income, currency, true, Modifier.weight(1f)); Metric("Расход", expense, currency, false, Modifier.weight(1f))
                    }
                }
            }
        }
        item { SectionTitle("Быстрый доступ") }
        item { Row(Modifier.padding(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) { QuickCard("Добавить расход", Icons.Default.Remove, Modifier.weight(1f)); QuickCard("Задать вопрос ИИ", Icons.Default.AutoAwesome, Modifier.weight(1f), onAi) } }
        item { SectionTitle("Состояние системы") }
        item {
            Row(Modifier.padding(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                MiniStat("Привычки", "${habits.count { it.completedToday }}/${habits.size}", Modifier.weight(1f))
                MiniStat("Цели", "${goals.count { it.completed }}/${goals.size}", Modifier.weight(1f))
                MiniStat("Курсы", if (fx == null) "—" else "обновлены", Modifier.weight(1f))
            }
        }
        item { SectionTitle("Последние операции") }
        items(tx.take(5)) { TransactionRow(it, currency) }
    }
}

@Composable private fun SectionTitle(text: String) { Text(text, fontWeight = FontWeight.SemiBold, fontSize = 18.sp, modifier = Modifier.padding(20.dp, 24.dp, 20.dp, 10.dp)) }

@Composable private fun Metric(label: String, value: Double, currency: String, positive: Boolean, modifier: Modifier) { Column(modifier) { Text(label, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = .7f), fontSize = 12.sp); Text((if (positive) "+" else "−") + formatMoney(value, currency), fontWeight = FontWeight.SemiBold) } }

@Composable private fun QuickCard(title: String, icon: androidx.compose.ui.graphics.vector.ImageVector, modifier: Modifier, onClick: (() -> Unit)? = null) { Card(modifier.clickable { onClick?.invoke() }, shape = SurfaceShape) { Column(Modifier.padding(16.dp)) { Icon(icon, null); Spacer(Modifier.height(14.dp)); Text(title, fontWeight = FontWeight.SemiBold) } } }
@Composable private fun MiniStat(label: String, value: String, modifier: Modifier) { Card(modifier, shape = RoundedCornerShape(18.dp)) { Column(Modifier.padding(14.dp)) { Text(label, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant); Text(value, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 5.dp)) } } }

@Composable
private fun Finances(tx: List<TransactionEntity>, currency: String) {
    val total = tx.filter { it.currency == currency }.sumOf { if (it.type == "income") it.amount else -it.amount }
    Header("Финансы", "Транзакции, группы и аналитика")
    LazyColumn(contentPadding = PaddingValues(bottom = 80.dp)) {
        item { Card(Modifier.padding(horizontal = 16.dp).fillMaxWidth(), shape = SurfaceShape) { Column(Modifier.padding(20.dp)) { Text("Баланс в $currency", color = MaterialTheme.colorScheme.onSurfaceVariant); Text(formatMoney(total, currency), fontSize = 28.sp, fontWeight = FontWeight.Bold); Text("Все операции автоматически учитываются в выбранной валюте.", fontSize = 13.sp, modifier = Modifier.padding(top = 8.dp)) } } }
        item { SectionTitle("Операции") }
        items(tx) { TransactionRow(it, currency) }
        if (tx.isEmpty()) item { EmptyState("Пока нет операций", "Нажмите + и добавьте первый доход или расход.") }
    }
}

@Composable private fun TransactionRow(t: TransactionEntity, base: String) { ListItem(headlineContent = { Text(t.title, fontWeight = FontWeight.Medium) }, supportingContent = { Text("${t.category} · ${t.currency}") }, trailingContent = { Text((if (t.type == "income") "+" else "−") + formatMoney(t.amount, t.currency), fontWeight = FontWeight.Bold) }, leadingContent = { Surface(shape = RoundedCornerShape(12.dp), color = if (t.type == "income") MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surfaceVariant) { Text(if (t.type == "income") "↑" else "↓", Modifier.padding(10.dp)) } }) }

@Composable
private fun Habits(items: List<HabitEntity>, vm: AstroViewModel) {
    Header("Привычки", "Ритм, серии и ежедневный контроль")
    LazyColumn(contentPadding = PaddingValues(bottom = 80.dp)) {
        items(items) { h -> Card(Modifier.padding(horizontal = 16.dp, vertical = 5.dp).fillMaxWidth().clickable { vm.toggleHabit(h) }, shape = RoundedCornerShape(20.dp)) { Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) { Checkbox(h.completedToday, { vm.toggleHabit(h) }); Column(Modifier.weight(1f)) { Text(h.title, fontWeight = FontWeight.SemiBold); Text("${h.cadence} · серия ${h.streak}", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp) }; Text(if (h.completedToday) "Готово" else "Сегодня", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Medium) } } }
        if (items.isEmpty()) item { EmptyState("Создайте привычку", "Например: тренировка, чтение, вода или сон.") }
    }
}

@Composable
private fun Goals(items: List<GoalEntity>, vm: AstroViewModel) {
    Header("Цели и задачи", "Сроки, прогресс и приоритеты")
    LazyColumn(contentPadding = PaddingValues(bottom = 80.dp)) {
        items(items) { g -> Card(Modifier.padding(horizontal = 16.dp, vertical = 5.dp).fillMaxWidth(), shape = RoundedCornerShape(20.dp)) { Column(Modifier.padding(17.dp)) { Row(verticalAlignment = Alignment.CenterVertically) { Checkbox(g.completed, { vm.toggleGoal(g) }); Text(g.title, Modifier.weight(1f), fontWeight = FontWeight.SemiBold) }; g.targetAmount?.let { Text("${formatMoney(g.currentAmount, g.currency)} / ${formatMoney(it, g.currency)}", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp) }; g.dueAt?.let { Text("До ${SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault()).format(Date(it))}", fontSize = 12.sp, modifier = Modifier.padding(top = 8.dp)) } } } }
        if (items.isEmpty()) item { EmptyState("Поставьте первую цель", "Можно задать сумму, дату и время.") }
    }
}

@Composable
private fun AiScreen(vm: AstroViewModel) {
    var q by remember { mutableStateOf("") }
    val answer by vm.aiAnswer.collectAsState(); val busy by vm.aiBusy.collectAsState()
    Header("Astro AI", "Ваш помощник по личной информации")
    Column(Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
        Card(Modifier.fillMaxWidth(), shape = SurfaceShape) { Column(Modifier.padding(20.dp)) { Text("Спросите что угодно", fontWeight = FontWeight.Bold, fontSize = 20.sp); Text("Например: «куда уходит больше всего денег?» или «какие привычки я чаще пропускаю?».", color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 6.dp)); OutlinedTextField(q, { q = it }, Modifier.fillMaxWidth().padding(top = 16.dp), placeholder = { Text("Ваш вопрос") }, maxLines = 4); Button(onClick = { vm.askAi(q) }, enabled = q.isNotBlank() && !busy, modifier = Modifier.fillMaxWidth().padding(top = 12.dp)) { Text(if (busy) "Думаю…" else "Спросить") } } }
        answer?.let { Card(Modifier.fillMaxWidth().padding(top = 14.dp), shape = RoundedCornerShape(20.dp)) { Text(it, Modifier.padding(18.dp)) } }
    }
}

@Composable
private fun Settings(vm: AstroViewModel, accent: Long) {
    val theme by vm.theme.collectAsState(); val nick by vm.nickname.collectAsState(); val currency by vm.baseCurrency.collectAsState(); val pin by vm.pinEnabled.collectAsState(); val avatar by vm.avatarUri.collectAsState()
    var pinDialog by remember { mutableStateOf(false) }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri -> uri?.let { vm.setAvatar(it.toString()) } }
    Header("Настройки", "Astro подстраивается под вас")
    LazyColumn(contentPadding = PaddingValues(16.dp, 0.dp, 16.dp, 80.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { SettingsCard("Профиль") { OutlinedTextField(nick, vm::setNickname, label = { Text("Никнейм") }, singleLine = true, modifier = Modifier.fillMaxWidth()); Spacer(Modifier.height(10.dp)); Row(verticalAlignment = Alignment.CenterVertically) { Button(onClick = { launcher.launch("image/*") }) { Text(if (avatar.isBlank()) "Выбрать аватар" else "Сменить аватар") }; if (avatar.isNotBlank()) Text("  ✓ выбран", color = MaterialTheme.colorScheme.primary) } } }
        item { SettingsCard("Внешний вид") { Text("Тема"); Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 8.dp)) { listOf("system" to "Система", "light" to "Светлая", "dark" to "Тёмная").forEach { (v,l) -> FilterChip(selected = theme == v, onClick = { vm.setTheme(v) }, label = { Text(l) }) } }; Text("Цвет акцента", Modifier.padding(top = 18.dp)); Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.padding(top = 8.dp)) { listOf(0xFF8B5CF6,0xFF06B6D4,0xFF22C55E,0xFFF97316,0xFFEC4899).forEach { c -> Box(Modifier.size(38.dp).clip(RoundedCornerShape(12.dp)).background(Color(c)).clickable { vm.setAccent(c) }) } }; Text("Градиенты и переливающиеся акценты предусмотрены архитектурой темы; статичный акцент включён по умолчанию.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 10.dp)) } }
        item { SettingsCard("Финансы") { OutlinedTextField(currency, { vm.setBaseCurrency(it.uppercase()) }, label = { Text("Базовая валюта") }, singleLine = true, modifier = Modifier.fillMaxWidth()); Text("Курсы обновляются автоматически через фоновую задачу при подключении к сети.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 8.dp)) } }
        item { SettingsCard("Безопасность") { Text(if (pin) "PIN-код включён" else "PIN-код выключен"); Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 10.dp)) { Button(onClick = { pinDialog = true }) { Text(if (pin) "Сменить PIN" else "Установить PIN") }; if (pin) OutlinedButton(onClick = vm::clearPin) { Text("Удалить") } } } }
        item { SettingsCard("О приложении") { Text("Astro 1.0.0", fontWeight = FontWeight.Bold); Text("Финансы • привычки • цели • ИИ", color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 4.dp)) } }
    }
    if (pinDialog) PinSetupDialog(vm) { pinDialog = false }
}

@Composable private fun SettingsCard(title: String, content: @Composable ColumnScope.() -> Unit) { Card(Modifier.fillMaxWidth(), shape = SurfaceShape) { Column(Modifier.padding(18.dp)) { Text(title, fontWeight = FontWeight.Bold, fontSize = 18.sp); Spacer(Modifier.height(12.dp)); content() } } }
@Composable private fun EmptyState(title: String, subtitle: String) { Box(Modifier.fillMaxWidth().padding(36.dp), contentAlignment = Alignment.Center) { Column(horizontalAlignment = Alignment.CenterHorizontally) { Icon(Icons.Default.AutoAwesome, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(40.dp)); Text(title, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 10.dp)); Text(subtitle, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 5.dp)) } } }

@Composable
private fun AddTransactionDialog(vm: AstroViewModel, close: () -> Unit) {
    var title by remember { mutableStateOf("") }; var amount by remember { mutableStateOf("") }; var currency by remember { mutableStateOf("EUR") }; var category by remember { mutableStateOf("Другое") }; var income by remember { mutableStateOf(false) }
    AstroDialog("Новая операция", close) { OutlinedTextField(title, { title = it }, label = { Text("Название") }, singleLine = true, modifier = Modifier.fillMaxWidth()); OutlinedTextField(amount, { amount = it }, label = { Text("Сумма") }, singleLine = true, modifier = Modifier.fillMaxWidth().padding(top = 8.dp)); Row(Modifier.padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) { OutlinedTextField(currency, { currency = it }, label = { Text("Валюта") }, singleLine = true, modifier = Modifier.weight(1f)); OutlinedTextField(category, { category = it }, label = { Text("Группа") }, singleLine = true, modifier = Modifier.weight(1.4f)) }; Row(verticalAlignment = Alignment.CenterVertically) { RadioButton(!income, { income = false }); Text("Расход"); RadioButton(income, { income = true }); Text("Доход") }; Button(onClick = { amount.toDoubleOrNull()?.let { vm.addTransaction(title.ifBlank { "Без названия" }, it, currency, category, income); close() } }, modifier = Modifier.fillMaxWidth()) { Text("Сохранить") } }
}

@Composable private fun AddHabitDialog(vm: AstroViewModel, close: () -> Unit) { var title by remember { mutableStateOf("") }; AstroDialog("Новая привычка", close) { OutlinedTextField(title, { title = it }, label = { Text("Название") }, singleLine = true, modifier = Modifier.fillMaxWidth()); Button(onClick = { if (title.isNotBlank()) { vm.addHabit(title); close() } }, modifier = Modifier.fillMaxWidth().padding(top = 12.dp)) { Text("Создать") } } }

@Composable
private fun AddGoalDialog(vm: AstroViewModel, close: () -> Unit) {
    val context = LocalContext.current; var title by remember { mutableStateOf("") }; var target by remember { mutableStateOf("") }; var currency by remember { mutableStateOf("EUR") }; var due by remember { mutableStateOf<Long?>(null) }
    AstroDialog("Цель или задача", close) {
        OutlinedTextField(title, { title = it }, label = { Text("Название") }, singleLine = true, modifier = Modifier.fillMaxWidth()); OutlinedTextField(target, { target = it }, label = { Text("Целевая сумма (необязательно)") }, singleLine = true, modifier = Modifier.fillMaxWidth().padding(top = 8.dp)); OutlinedTextField(currency, { currency = it }, label = { Text("Валюта") }, singleLine = true, modifier = Modifier.fillMaxWidth().padding(top = 8.dp))
        OutlinedButton(onClick = { val now = Calendar.getInstance(); DatePickerDialog(context, { _, y,m,d -> val c = Calendar.getInstance(); c.set(y,m,d); TimePickerDialog(context, { _,h,min -> c.set(Calendar.HOUR_OF_DAY,h); c.set(Calendar.MINUTE,min); due = c.timeInMillis }, now.get(Calendar.HOUR_OF_DAY), now.get(Calendar.MINUTE), true).show() }, now.get(Calendar.YEAR), now.get(Calendar.MONTH), now.get(Calendar.DAY_OF_MONTH)).show() }, modifier = Modifier.fillMaxWidth().padding(top = 10.dp)) { Text(due?.let { "Срок: ${SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault()).format(Date(it))}" } ?: "Задать дату и время") }
        Button(onClick = { if (title.isNotBlank()) { vm.addGoal(title, target.toDoubleOrNull(), currency); close() } }, modifier = Modifier.fillMaxWidth().padding(top = 10.dp)) { Text("Создать") }
    }
}

@Composable private fun AstroDialog(title: String, close: () -> Unit, content: @Composable ColumnScope.() -> Unit) { AlertDialog(onDismissRequest = close, title = { Text(title) }, text = { Column(content = content) }, confirmButton = {}, dismissButton = { TextButton(onClick = close) { Text("Закрыть") } }) }

@Composable
private fun AccountDialog(vm: AstroViewModel, close: () -> Unit) {
    var email by remember { mutableStateOf("") }; var password by remember { mutableStateOf("") }; var code by remember { mutableStateOf("") }; var signup by remember { mutableStateOf(false) }; val msg by vm.authMessage.collectAsState(); val logged by vm.loggedIn.collectAsState()
    AlertDialog(onDismissRequest = close, title = { Text(if (logged) "Аккаунт Astro" else if (signup) "Регистрация" else "Вход") }, text = {
        Column { if (logged) { Text("Аккаунт подключен", fontWeight = FontWeight.Bold); Button(onClick = { vm.logout(); close() }, modifier = Modifier.fillMaxWidth().padding(top = 14.dp)) { Text("Выйти") } } else { OutlinedTextField(email, { email = it }, label = { Text("Email") }, singleLine = true, modifier = Modifier.fillMaxWidth()); OutlinedTextField(password, { password = it }, label = { Text("Пароль") }, visualTransformation = PasswordVisualTransformation(), singleLine = true, modifier = Modifier.fillMaxWidth().padding(top = 8.dp)); AnimatedVisibility(signup) { OutlinedTextField(code, { code = it }, label = { Text("Код из письма") }, singleLine = true, modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) }; msg?.let { Text(it, fontSize = 12.sp, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(top = 8.dp)) }; Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 14.dp)) { Button(onClick = { if (signup && code.isNotBlank()) vm.verify(email, code) else if (signup) vm.signup(email,password) else vm.login(email,password) }, modifier = Modifier.weight(1f)) { Text(if (signup) if (code.isBlank()) "Получить код" else "Подтвердить" else "Войти") }; TextButton(onClick = { signup = !signup; vm.clearAuthMessage() }) { Text(if (signup) "Вход" else "Регистрация") } } } }
    }, confirmButton = {}, dismissButton = { TextButton(onClick = close) { Text("Закрыть") } })
}

@Composable
private fun PinSetupDialog(vm: AstroViewModel, close: () -> Unit) {
    var pin by remember { mutableStateOf("") }
    AlertDialog(onDismissRequest = close, title = { Text("PIN-код") }, text = { OutlinedTextField(pin, { if (it.length <= 4 && it.all(Char::isDigit)) pin = it }, label = { Text("4 цифры") }, visualTransformation = PasswordVisualTransformation(), singleLine = true, modifier = Modifier.fillMaxWidth()) }, confirmButton = { Button(onClick = { if (pin.length == 4) { vm.setPin(pin); close() } }, enabled = pin.length == 4) { Text("Сохранить") } }, dismissButton = { TextButton(onClick = close) { Text("Отмена") } })
}

@Composable
private fun PinLock(vm: AstroViewModel, onUnlock: () -> Unit) {
    var pin by remember { mutableStateOf("") }; var error by remember { mutableStateOf(false) }; val scope = rememberCoroutineScope()
    Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background), contentAlignment = Alignment.Center) {
        Card(Modifier.padding(24.dp), shape = SurfaceShape) { Column(Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) { Text("Astro", fontSize = 30.sp, fontWeight = FontWeight.Bold); Text("Введите PIN-код", color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 6.dp)); OutlinedTextField(pin, { if (it.length <= 4 && it.all(Char::isDigit)) pin = it; error = false }, label = { Text("PIN") }, visualTransformation = PasswordVisualTransformation(), singleLine = true, modifier = Modifier.fillMaxWidth().padding(top = 18.dp)); if (error) Text("Неверный PIN", color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(top = 8.dp)); Button(onClick = { scope.launch { if (vm.verifyPin(pin)) onUnlock() else error = true } }, enabled = pin.length == 4, modifier = Modifier.fillMaxWidth().padding(top = 14.dp)) { Text("Открыть Astro") } } }
    }
}

private fun formatMoney(value: Double, currency: String): String { val f = NumberFormat.getNumberInstance(Locale.getDefault()); f.maximumFractionDigits = 2; f.minimumFractionDigits = 0; return "${f.format(value)} $currency" }
