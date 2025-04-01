package com.example.team26.ui.station

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun SearchAndFilterBar(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    filterField: FilterField,
    onFilterFieldChange: (FilterField) -> Unit,
    sortOrder: SortOrder,
    onSortOrderChange: (SortOrder) -> Unit,
    modifier: Modifier = Modifier
) {
    var showFilterMenu by remember { mutableStateOf(false) }

    Row(
        modifier = modifier.fillMaxWidth().padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = { showFilterMenu = true }) {
            Icon(Icons.Filled.FilterList, contentDescription = "Filter")
        }
        TextField(
            value = searchQuery,
            onValueChange = onSearchQueryChange,
            label = { Text("Search by name") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            modifier = Modifier.weight(1f)
        )
        DropdownMenu(
            expanded = showFilterMenu,
            onDismissRequest = { showFilterMenu = false }
        ) {
            Text("Filter by:", modifier = Modifier.padding(8.dp))
            FilterField.entries.forEach { field ->
                DropdownMenuItem(
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(
                                selected = filterField == field,
                                onClick = { onFilterFieldChange(field) }
                            )
                            Text(field.displayName)
                        }
                    },
                    onClick = {
                        onFilterFieldChange(field)
                        showFilterMenu = false
                    }
                )
            }
            HorizontalDivider()
            Text("Sort order:", modifier = Modifier.padding(8.dp))
            SortOrder.entries.forEach { order ->
                DropdownMenuItem(
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(
                                selected = sortOrder == order,
                                onClick = { onSortOrderChange(order) }
                            )
                            Text(order.displayName)
                        }
                    },
                    onClick = {
                        onSortOrderChange(order)
                        showFilterMenu = false
                    }
                )
            }
        }
    }
}