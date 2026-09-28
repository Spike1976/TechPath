package com.techpath.learn.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.techpath.learn.data.CatalogRepository
import com.techpath.learn.data.ProgressStore
import com.techpath.learn.engine.LearningEngine
import com.techpath.learn.model.*

private enum class Tab(val label:String) { HOME("Home"), LEARN("Learn"), BUILD("Build"), LAB("Lab"), PROGRESS("Progress") }
private sealed interface Screen {
    data object Root:Screen
    data class Category(val id:String):Screen
    data class Concept(val id:String):Screen
    data class Project(val id:String):Screen
    data object Assessment:Screen
}

@Composable
fun TechPathApp() {
    val context=LocalContext.current
    val catalog=remember { CatalogRepository(context).load() }
    val store=remember { ProgressStore(context) }
    var progress by remember { mutableStateOf(store.load()) }
    var tab by remember { mutableStateOf(Tab.HOME) }
    var screen by remember { mutableStateOf<Screen>(Screen.Root) }
    fun save(p:UserProgress){ progress=p; store.save(p) }

    Scaffold(bottomBar={
        NavigationBar {
            Tab.entries.forEach { item ->
                NavigationBarItem(
                    selected=tab==item && screen==Screen.Root,
                    onClick={ tab=item; screen=Screen.Root },
                    icon={ Icon(tabIcon(item),item.label) },
                    label={ Text(item.label) }
                )
            }
        }
    }) { pad ->
        Box(Modifier.fillMaxSize().padding(pad)) {
            when(val s=screen){
                Screen.Root -> when(tab){
                    Tab.HOME -> Home(catalog,progress,{screen=Screen.Project(it)},{screen=Screen.Concept(it)},{screen=Screen.Assessment})
                    Tab.LEARN -> Learn(catalog,progress,{screen=Screen.Category(it)},{screen=Screen.Concept(it)})
                    Tab.BUILD -> Build(catalog,progress,{screen=Screen.Project(it)},{save(progress.copy(selectedProjectId=it))})
                    Tab.LAB -> Lab({screen=Screen.Concept(it)})
                    Tab.PROGRESS -> Progress(catalog,progress)
                }
                is Screen.Category -> CategoryView(catalog,s.id,progress,{screen=Screen.Root},{screen=Screen.Concept(it)})
                is Screen.Concept -> ConceptView(catalog,s.id,progress,{screen=Screen.Root},{screen=Screen.Concept(it)},
                    {save(progress.copy(started=progress.started+it))},
                    {save(progress.copy(mastered=progress.mastered+it,started=progress.started+it))})
                is Screen.Project -> ProjectView(catalog,s.id,progress,{screen=Screen.Root},{screen=Screen.Concept(it)},
                    {save(progress.copy(selectedProjectId=it))})
                Screen.Assessment -> Assessment(progress,{screen=Screen.Root}) {
                    save(progress.copy(mastered=progress.mastered+it)); screen=Screen.Root
                }
            }
        }
    }
}

private fun tabIcon(tab:Tab)=when(tab){
    Tab.HOME->Icons.Default.Home
    Tab.LEARN->Icons.Default.School
    Tab.BUILD->Icons.Default.Build
    Tab.LAB->Icons.Default.Science
    Tab.PROGRESS->Icons.Default.Assessment
}

@Composable
private fun Page(title:String, subtitle:String?=null, back:(()->Unit)?=null, body:@Composable ColumnScope.()->Unit){
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(18.dp),verticalArrangement=Arrangement.spacedBy(14.dp)){
        Row(verticalAlignment=Alignment.CenterVertically){
            if(back!=null) IconButton(onClick=back){Icon(Icons.Default.ArrowBack,"Back")}
            Column {
                Text(title,fontWeight=FontWeight.Bold,fontSize=28.sp)
                if(subtitle!=null) Text(subtitle,color=MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        body()
        Spacer(Modifier.height(20.dp))
    }
}

@Composable
private fun Card(title:String, body:@Composable ColumnScope.()->Unit){
    ElevatedCard(Modifier.fillMaxWidth()){
        Column(Modifier.padding(16.dp),verticalArrangement=Arrangement.spacedBy(9.dp)){
            Text(title,fontWeight=FontWeight.Bold,fontSize=18.sp)
            body()
        }
    }
}

@Composable private fun Bullet(text:String){ Row { Text("•  ",color=MaterialTheme.colorScheme.primary); Text(text,Modifier.weight(1f)) } }
@Composable private fun Step(n:Int,text:String){ Row { Text(n.toString(),Modifier.width(30.dp),color=MaterialTheme.colorScheme.primary,fontWeight=FontWeight.Bold); Text(text,Modifier.weight(1f)) } }

@Composable
private fun Home(catalog:Catalog,p:UserProgress,openProject:(String)->Unit,openConcept:(String)->Unit,assessment:()->Unit){
    val goal=catalog.projects.firstOrNull{it.id==p.selectedProjectId}
    Page("TechPath","Learn how technology works. Then build your own."){
        Button(assessment,Modifier.fillMaxWidth()){Text("Find my real starting point")}
        if(goal!=null){
            val ready=LearningEngine.readiness(goal,p)
            val next=LearningEngine.nextConcept(goal,catalog,p)
            Card("Current goal"){
                Text(goal.title,fontSize=20.sp,fontWeight=FontWeight.SemiBold)
                LinearProgressIndicator({ready/100f},Modifier.fillMaxWidth())
                Text(ready.toString()+"% path mastery")
                if(next!=null) Button({openConcept(next.id)}){Text("Continue: "+next.title)}
                else Text("Path complete. Every mapped skill is mastered.")
                TextButton({openProject(goal.id)}){Text("View full path")}
            }
        } else Card("Start with a goal"){
            Text("Pick something you want to build or understand. TechPath maps prerequisites, skips demonstrated knowledge, and keeps every lesson tied to the goal.")
            catalog.projects.take(3).forEach { q -> OutlinedButton({openProject(q.id)},Modifier.fillMaxWidth()){Text(q.title)} }
        }
        Card("How learning works"){
            Bullet("Understand the mental model before memorizing syntax.")
            Bullet("See why the concept matters to your goal.")
            Bullet("Predict what should happen before answers are shown.")
            Bullet("Do a hands-on task or safe simulation.")
            Bullet("Prove understanding with a practical knowledge check.")
            Bullet("Follow explicit prerequisite-aware next steps.")
        }
    }
}

@Composable
private fun Learn(catalog:Catalog,p:UserProgress,openCategory:(String)->Unit,openConcept:(String)->Unit){
    var query by remember { mutableStateOf("") }
    val found=remember(query){LearningEngine.search(catalog,query)}
    Page("Learn","Curated technical knowledge with a reason to learn it."){
        OutlinedTextField(query,{query=it},Modifier.fillMaxWidth(),label={Text("Search concepts or uses")},leadingIcon={Icon(Icons.Default.Search,null)})
        if(query.isNotBlank()){
            found.forEach{ConceptRow(it,p,openConcept)}
            if(found.isEmpty()) Text("No match in the curated catalog.")
        } else catalog.categories.forEach { c ->
            val count=catalog.concepts.count{it.categoryId==c.id}
            ElevatedCard({openCategory(c.id)},Modifier.fillMaxWidth()){
                Column(Modifier.padding(16.dp)){
                    Text(c.icon+"  "+c.title,fontWeight=FontWeight.Bold,fontSize=19.sp)
                    Text(c.description,color=MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(count.toString()+" learnable concepts",color=MaterialTheme.colorScheme.primary)
                }
            }
        }
    }
}

@Composable
private fun CategoryView(catalog:Catalog,id:String,p:UserProgress,back:()->Unit,open:(String)->Unit){
    val category=catalog.categories.first{it.id==id}
    Page(category.title,category.description,back){
        catalog.concepts.filter{it.categoryId==id}.forEach{ConceptRow(it,p,open)}
    }
}

@Composable
private fun ConceptRow(c:Concept,p:UserProgress,open:(String)->Unit){
    ElevatedCard({open(c.id)},Modifier.fillMaxWidth()){
        Row(Modifier.padding(14.dp),verticalAlignment=Alignment.CenterVertically){
            val mark=when{c.id in p.mastered->"✓";c.id in p.started->"◐";else->"○"}
            Text(mark,fontSize=22.sp,color=MaterialTheme.colorScheme.primary)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)){Text(c.title,fontWeight=FontWeight.SemiBold);Text(c.summary,maxLines=2,color=MaterialTheme.colorScheme.onSurfaceVariant)}
            Icon(Icons.Default.ChevronRight,null)
        }
    }
}

@Composable
private fun ConceptView(catalog:Catalog,id:String,p:UserProgress,back:()->Unit,open:(String)->Unit,started:(String)->Unit,mastered:(String)->Unit){
    val c=catalog.concepts.first{it.id==id}
    val missing=LearningEngine.missingPrerequisites(c,p)
    LaunchedEffect(id){started(id)}
    Page(c.title,c.summary,back){
        if(missing.isNotEmpty()) Card("Learn these first"){
            Text("You may explore this lesson, but these prerequisites are not mastered:")
            missing.mapNotNull{m->catalog.concepts.firstOrNull{it.id==m}}.forEach{q->TextButton({open(q.id)}){Text("→ "+q.title)}}
        }
        Card("Why this matters"){Text(c.whyItMatters)}
        Card("Learn it step by step"){c.learnSteps.forEachIndexed{i,s->Step(i+1,s)}}
        Card("Hands-on"){Text(c.handsOn);Text("Predict the result and explain why before checking the answer.",color=MaterialTheme.colorScheme.tertiary)}
        Card("Real-world uses"){c.realWorldUses.forEach{Bullet(it)}}
        c.safetyNote?.let { note -> Card("Safety & permission"){Text(note)} }
        KnowledgeCheck(c,c.id in p.mastered){mastered(c.id)}
        val next=LearningEngine.recommendedNext(c,catalog)
        if(next.isNotEmpty()) Card("Next steps"){next.forEach{q->TextButton({open(q.id)}){Text("→ "+q.title)}}}
    }
}

@Composable
private fun KnowledgeCheck(c:Concept,isMastered:Boolean,onMaster:()->Unit){
    var answer by remember(c.id){mutableStateOf("")}
    var reveal by remember(c.id){mutableStateOf(false)}
    Card("Prove you understand it"){
        Text(c.checkQuestion,fontWeight=FontWeight.Medium)
        OutlinedTextField(answer,{answer=it},Modifier.fillMaxWidth(),minLines=3,label={Text("Explain in your own words")})
        Button({reveal=true},enabled=answer.trim().length>=20){Text("Check my reasoning")}
        if(isMastered) Text("Mastered ✓",color=MaterialTheme.colorScheme.secondary)
        if(reveal){
            Text("A strong answer should include:",fontWeight=FontWeight.Bold)
            Text(c.checkAnswer)
            Text("Compare the reasoning, not the wording. Master it only if you can explain and use the idea.")
            Button(onMaster){Text("I can explain and use this")}
        }
    }
}

@Composable
private fun Build(catalog:Catalog,p:UserProgress,open:(String)->Unit,select:(String)->Unit){
    Page("Build","The project is the reason. The lessons are the route."){
        catalog.projects.forEach { q ->
            val ready=LearningEngine.readiness(q,p)
            ElevatedCard({open(q.id)},Modifier.fillMaxWidth()){
                Column(Modifier.padding(16.dp),verticalArrangement=Arrangement.spacedBy(8.dp)){
                    Text(q.title,fontWeight=FontWeight.Bold,fontSize=19.sp)
                    Text(q.description,color=MaterialTheme.colorScheme.onSurfaceVariant)
                    LinearProgressIndicator({ready/100f},Modifier.fillMaxWidth())
                    Text(ready.toString()+"% mastered • "+q.conceptIds.size+" mapped skills")
                    if(p.selectedProjectId!=q.id) TextButton({select(q.id)}){Text("Make this my goal")} else Text("Current goal ✓",color=MaterialTheme.colorScheme.secondary)
                }
            }
        }
    }
}

@Composable
private fun ProjectView(catalog:Catalog,id:String,p:UserProgress,back:()->Unit,open:(String)->Unit,select:(String)->Unit){
    val q=catalog.projects.first{it.id==id}
    val map=catalog.concepts.associateBy{it.id}
    val ready=LearningEngine.readiness(q,p)
    val next=LearningEngine.nextConcept(q,catalog,p)
    Page(q.title,q.description,back){
        Card("Outcome"){Text(q.outcome)}
        Card("Your readiness"){
            LinearProgressIndicator({ready/100f},Modifier.fillMaxWidth())
            Text(ready.toString()+"% • "+q.conceptIds.count{it in p.mastered}+" of "+q.conceptIds.size+" skills mastered")
            if(p.selectedProjectId!=id) Button({select(id)}){Text("Make this my goal")}
            if(next!=null) Button({open(next.id)}){Text("Continue: "+next.title)}
        }
        Text("Learning path",fontWeight=FontWeight.Bold,fontSize=20.sp)
        q.conceptIds.mapNotNull(map::get).forEachIndexed { i,c ->
            Row(Modifier.fillMaxWidth().clickable{open(c.id)}.padding(vertical=8.dp),verticalAlignment=Alignment.CenterVertically){
                Text((i+1).toString(),Modifier.width(34.dp),color=MaterialTheme.colorScheme.primary,fontWeight=FontWeight.Bold)
                Column(Modifier.weight(1f)){Text(c.title);Text(when{c.id in p.mastered->"Mastered";c.id in p.started->"In progress";else->"Not started"},color=MaterialTheme.colorScheme.onSurfaceVariant)}
                Icon(Icons.Default.ChevronRight,null)
            }
        }
    }
}

@Composable
private fun Lab(open:(String)->Unit){
    Page("Lab","Use the Android phone as part of the classroom."){
        LabCard("Network Mental Model","Trace phone → Wi-Fi → device → service → data.","networking-what-a-network-is",open)
        LabCard("BLE Service Explorer","Learn scan → connect → services → characteristics on hardware you own.","protocols-bluetooth-low-energy",open)
        LabCard("USB / Serial Lab","Learn Android USB host mode and serial bridges.","android-android-usb-host",open)
        LabCard("ESP32 Connection Lab","Follow GPIO and sensors through Wi-Fi to an Android dashboard.","esp32-esp32-sensor-integration",open)
        LabCard("Owner Device Analysis","Learn authorization, passive observation, logging, and protocol identification.","security-authorization-and-scope",open)
        Card("Live instruments planned next"){
            Bullet("Bluetooth LE scanner and characteristic viewer")
            Bullet("USB serial terminal")
            Bullet("Phone sensor scope")
            Bullet("User-owned device API inspector")
            Bullet("Data logger and chart builder")
            Bullet("Camera-assisted component identification")
        }
    }
}

@Composable
private fun LabCard(title:String,desc:String,id:String,open:(String)->Unit){
    ElevatedCard({open(id)},Modifier.fillMaxWidth()){Column(Modifier.padding(16.dp)){Text(title,fontWeight=FontWeight.Bold,fontSize=18.sp);Text(desc);Text("Open learning path →",color=MaterialTheme.colorScheme.primary)}}
}

private data class Diagnostic(val id:String,val prompt:String,val answers:List<String>,val correct:String)
private val diagnostics=listOf(
    Diagnostic("programming-values-and-data-types","Which item is a boolean value?",listOf("72.4","true","\"true\"","[true]"),"true"),
    Diagnostic("programming-variables","What is the main purpose of a variable?",listOf("Name and store a value","Repeat code","Connect to Wi-Fi","Draw a chart"),"Name and store a value"),
    Diagnostic("programming-conditions","A battery warning appears only below a voltage limit. Which idea is central?",listOf("Condition","Loop","File","Class"),"Condition"),
    Diagnostic("programming-loops","You must process 500 sensor readings the same way. What avoids writing the operation 500 times?",listOf("Loop","Comment","Port","Regulator"),"Loop"),
    Diagnostic("electronics-voltage","Voltage is best described as...",listOf("Electrical potential difference","Rate of charge flow","Opposition to current","Stored code"),"Electrical potential difference"),
    Diagnostic("electronics-current","Electrical current describes...",listOf("Rate of charge flow","Network address","Frequency only","Storage capacity"),"Rate of charge flow"),
    Diagnostic("networking-ip-addresses","What does an IP address identify for IP communication?",listOf("A network endpoint/interface","A language","A voltage","A file format"),"A network endpoint/interface"),
    Diagnostic("networking-ports","Why can one IP address provide several services?",listOf("Ports identify services","MAC executes programs","DNS stores files","Voltage separates traffic"),"Ports identify services"),
    Diagnostic("networking-tcp","Which property is associated with TCP?",listOf("Ordered reliable byte stream","No delivery tracking","Analog conversion","Radio selection"),"Ordered reliable byte stream"),
    Diagnostic("protocols-uart","TX from one UART device normally connects to...",listOf("RX on the other device","TX only","Battery positive","USB shield"),"RX on the other device"),
    Diagnostic("protocols-bluetooth-low-energy","BLE commonly organizes exposed data as...",listOf("Services and characteristics","Folders and drives","Rows only","CAN IDs"),"Services and characteristics"),
    Diagnostic("programming-json","Which description best fits JSON?",listOf("Structured text data format","Radio modulation","Battery connector","Android permission"),"Structured text data format"),
    Diagnostic("android-jetpack-compose","Jetpack Compose primarily helps build...",listOf("Android user interfaces from Kotlin","CAN hardware","Antennas","Battery packs"),"Android user interfaces from Kotlin"),
    Diagnostic("security-authorization-and-scope","Before analyzing a device or network, the safest first question is...",listOf("Do I own it or have explicit permission?","Can I bypass login?","How fast can I scan it?","Can I hide activity?"),"Do I own it or have explicit permission?")
)

@Composable
private fun Assessment(p:UserProgress,back:()->Unit,finish:(Set<String>)->Unit){
    val questions=remember{diagnostics.shuffled()}
    var index by remember{mutableIntStateOf(0)}
    var selected by remember{mutableStateOf<String?>(null)}
    val correct=remember{mutableStateListOf<String>()}
    val q=questions[index]
    val choices=remember(index){q.answers.shuffled()}
    Page("Starting-point assessment","No lesson answers or hints are shown during the diagnostic.",back){
        LinearProgressIndicator({index.toFloat()/questions.size},Modifier.fillMaxWidth())
        Text("Question "+(index+1)+" of "+questions.size,color=MaterialTheme.colorScheme.primary)
        Card("Question"){
            Text(q.prompt,fontSize=19.sp,fontWeight=FontWeight.Medium)
            choices.forEach { a -> OutlinedButton({selected=a},Modifier.fillMaxWidth(),border=BorderStroke(1.dp,if(selected==a)MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline)){Text(a)} }
        }
        Button({
            if(selected==q.correct) correct.add(q.id)
            if(index==questions.lastIndex) finish(correct.toSet()) else {index++;selected=null}
        },Modifier.fillMaxWidth(),enabled=selected!=null){Text(if(index==questions.lastIndex)"Finish assessment" else "Next question")}
        Text("Only skills directly demonstrated by correct answers are marked mastered.",color=MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun Progress(catalog:Catalog,p:UserProgress){
    Page("Progress","Mastery is earned by explanation and use, not by opening a screen."){
        Card("Overall"){
            val pct=if(catalog.concepts.isEmpty())0 else p.mastered.size*100/catalog.concepts.size
            Text(p.mastered.size.toString()+" / "+catalog.concepts.size+" concepts mastered",fontWeight=FontWeight.Bold)
            LinearProgressIndicator({pct/100f},Modifier.fillMaxWidth())
            Text((p.started.size-p.mastered.size).coerceAtLeast(0).toString()+" in progress")
        }
        catalog.categories.forEach { c ->
            val ids=catalog.concepts.filter{it.categoryId==c.id}.map{it.id}
            val done=ids.count{it in p.mastered}
            val pct=if(ids.isEmpty())0 else done*100/ids.size
            Column(Modifier.fillMaxWidth()){
                Row{Text(c.title,Modifier.weight(1f));Text(done.toString()+"/"+ids.size)}
                LinearProgressIndicator({pct/100f},Modifier.fillMaxWidth())
            }
        }
    }
}
