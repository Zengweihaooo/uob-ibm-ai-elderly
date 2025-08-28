#!/bin/bash

echo "🧹 Final Markdown File Cleanup"
echo "============================="

# Create docs subdirectories
mkdir -p docs/documentation
mkdir -p docs/guides
mkdir -p docs/reports
mkdir -p docs/implementation
mkdir -p docs/meetings
mkdir -p docs/learning
mkdir -p docs/research
mkdir -p docs/architecture
mkdir -p docs/development
mkdir -p docs/testing
mkdir -p docs/deployment
mkdir -p docs/api
mkdir -p docs/user-guides
mkdir -p docs/technical-notes

echo "📁 Ensuring directory structure exists..."

# Function to categorize file
categorize_file() {
    local file_path="$1"
    local file_name=$(basename "$file_path")
    local dir_name=$(dirname "$file_path")
    
    # Skip files in docs directory
    if [[ "$dir_name" == docs* ]]; then
        return
    fi
    
    # Skip files in node_modules, .git, etc.
    if [[ "$dir_name" == *node_modules* ]] || [[ "$dir_name" == *.git* ]] || [[ "$dir_name" == *venv* ]]; then
        return
    fi
    
    # Determine category based on filename and directory
    local category=""
    
    # Check filename patterns
    if [[ "$file_name" == *README* ]] || [[ "$file_name" == *readme* ]]; then
        category="documentation"
    elif [[ "$file_name" == *GUIDE* ]] || [[ "$file_name" == *guide* ]] || [[ "$file_name" == *USAGE* ]] || [[ "$file_name" == *usage* ]]; then
        category="user-guides"
    elif [[ "$file_name" == *START* ]] || [[ "$file_name" == *start* ]] || [[ "$file_name" == *HOW* ]] || [[ "$file_name" == *how* ]]; then
        category="guides"
    elif [[ "$file_name" == *IMPLEMENTATION* ]] || [[ "$file_name" == *implementation* ]]; then
        category="implementation"
    elif [[ "$file_name" == *ARCHITECTURE* ]] || [[ "$file_name" == *architecture* ]]; then
        category="architecture"
    elif [[ "$file_name" == *API* ]] || [[ "$file_name" == *api* ]]; then
        category="api"
    elif [[ "$file_name" == *TEST* ]] || [[ "$file_name" == *test* ]]; then
        category="testing"
    elif [[ "$file_name" == *DEPLOY* ]] || [[ "$file_name" == *deploy* ]]; then
        category="deployment"
    elif [[ "$file_name" == *RESEARCH* ]] || [[ "$file_name" == *research* ]]; then
        category="research"
    elif [[ "$file_name" == *MEETING* ]] || [[ "$file_name" == *meeting* ]]; then
        category="meetings"
    elif [[ "$file_name" == *LEARNING* ]] || [[ "$file_name" == *learning* ]] || [[ "$file_name" == *JOURNAL* ]] || [[ "$file_name" == *journal* ]]; then
        category="learning"
    elif [[ "$file_name" == *TECHNICAL* ]] || [[ "$file_name" == *technical* ]]; then
        category="technical-notes"
    elif [[ "$file_name" == *DEVELOPMENT* ]] || [[ "$file_name" == *development* ]]; then
        category="development"
    elif [[ "$file_name" == *REPORT* ]] || [[ "$file_name" == *report* ]]; then
        category="reports"
    else
        # Default category based on directory
        if [[ "$dir_name" == *springboot* ]]; then
            category="implementation"
        elif [[ "$dir_name" == *commit-logs* ]]; then
            category="development"
        elif [[ "$dir_name" == *memoir_md* ]]; then
            category="documentation"
        elif [[ "$dir_name" == *release-notes* ]]; then
            category="documentation"
        else
            category="documentation"
        fi
    fi
    
    echo "$category"
}

# Find all remaining markdown files
echo "🔍 Finding all remaining markdown files..."
remaining_files=($(find . -name "*.md" -type f 2>/dev/null | grep -v "node_modules" | grep -v ".git" | grep -v "venv" | grep -v "docs"))

echo "📊 Found ${#remaining_files[@]} remaining markdown files"

# Process each file
processed_count=0
skipped_count=0

for file_path in "${remaining_files[@]}"; do
    echo "Processing: $file_path"
    
    # Skip if file doesn't exist
    if [ ! -f "$file_path" ]; then
        echo "  ⏭️  Skipped: File not found"
        ((skipped_count++))
        continue
    fi
    
    # Determine category
    category=$(categorize_file "$file_path")
    
    # Create target directory
    target_dir="docs/$category"
    mkdir -p "$target_dir"
    
    # Generate target filename
    file_name=$(basename "$file_path")
    target_file="$target_dir/$file_name"
    
    # Handle duplicate filenames
    counter=1
    original_target="$target_file"
    while [ -f "$target_file" ]; do
        name_without_ext="${file_name%.*}"
        ext="${file_name##*.}"
        target_file="$target_dir/${name_without_ext}_${counter}.${ext}"
        ((counter++))
    done
    
    # Move file (not copy)
    mv "$file_path" "$target_file"
    echo "  ✅ Moved to: $target_file"
    ((processed_count++))
done

echo ""
echo "📋 Processing Summary:"
echo "====================="
echo "📊 Total files found: ${#remaining_files[@]}"
echo "✅ Files moved: $processed_count"
echo "⏭️  Files skipped: $skipped_count"

# Show final structure
echo ""
echo "📁 Final Documentation Structure:"
echo "================================"
total_files=$(find docs -type f -name "*.md" | wc -l)
echo "Total markdown files in docs: $total_files"

# Check if any files remain
remaining_after=$(find . -name "*.md" -type f 2>/dev/null | grep -v "node_modules" | grep -v ".git" | grep -v "venv" | grep -v "docs" | wc -l)
echo "Remaining markdown files outside docs: $remaining_after"

echo ""
echo "🎉 Final cleanup complete!"
echo ""
echo "📋 Summary:"
echo "   - Moved $processed_count markdown files to docs/"
echo "   - Total files in docs: $total_files"
echo "   - Remaining files outside docs: $remaining_after"
echo ""
echo "💡 Next steps:"
echo "   1. Review the final structure in docs/"
echo "   2. Update any internal links if needed"
echo "   3. Commit the organized documentation structure"

