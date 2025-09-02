#!/bin/bash

echo "🧹 Document Cleanup Script"
echo "========================="

echo "This script will help you clean up original markdown files from the root directory"
echo "after they have been organized into the docs/ folder."
echo ""

# List original files that have been copied to docs/
echo "📋 Original files that have been organized:"
echo "=========================================="

original_files=(
    "README.md"
    "ONBOARDING_README.md"
    "MEMO_IMPLEMENTATION_SUMMARY.md"
    "IMPORTANT_DATES_IMPLEMENTATION.md"
    "TECHNICAL_ARCHITECTURE.md"
    "MEMO_README.md"
    "MEMO_USAGE_GUIDE.md"
    "QUICK_START.md"
    "HOW_TO_START.md"
    "TTS_STT_RESEARCH_REPORT.md"
)

for file in "${original_files[@]}"; do
    if [ -f "$file" ]; then
        echo "✅ $file (exists in root)"
    else
        echo "❌ $file (not found in root)"
    fi
done

echo ""
echo "📁 Corresponding organized files in docs/:"
echo "========================================="

organized_files=(
    "docs/documentation/README.md"
    "docs/documentation/ONBOARDING_README.md"
    "docs/implementation/MEMO_IMPLEMENTATION_SUMMARY.md"
    "docs/implementation/IMPORTANT_DATES_IMPLEMENTATION.md"
    "docs/implementation/TECHNICAL_ARCHITECTURE.md"
    "docs/guides/MEMO_README.md"
    "docs/guides/MEMO_USAGE_GUIDE.md"
    "docs/guides/QUICK_START.md"
    "docs/guides/HOW_TO_START.md"
    "docs/reports/TTS_STT_RESEARCH_REPORT.md"
)

for file in "${organized_files[@]}"; do
    if [ -f "$file" ]; then
        echo "✅ $file"
    else
        echo "❌ $file (missing)"
    fi
done

echo ""
echo "⚠️  Warning: This will permanently delete files from the root directory!"
echo ""

# Ask for confirmation
read -p "Do you want to remove original files from root directory? (y/N): " -n 1 -r
echo

if [[ $REPLY =~ ^[Yy]$ ]]; then
    echo ""
    echo "🗑️  Removing original files..."
    echo "============================="
    
    removed_count=0
    for file in "${original_files[@]}"; do
        if [ -f "$file" ]; then
            rm "$file"
            echo "✅ Removed: $file"
            ((removed_count++))
        else
            echo "⏭️  Skipped: $file (not found)"
        fi
    done
    
    echo ""
    echo "🎉 Cleanup complete!"
    echo "📊 Removed $removed_count files from root directory"
    echo ""
    echo "💡 All documentation is now organized in the docs/ folder"
    echo "   - Main index: docs/README.md"
    echo "   - Documentation: docs/documentation/"
    echo "   - Guides: docs/guides/"
    echo "   - Implementation: docs/implementation/"
    echo "   - Reports: docs/reports/"
    
else
    echo ""
    echo "✅ Cleanup cancelled. Original files remain in root directory."
    echo ""
    echo "💡 You can run this script again later when you're ready to clean up."
fi

echo ""
echo "📝 Next steps:"
echo "   1. Update any internal links that reference root-level files"
echo "   2. Update .gitignore if needed"
echo "   3. Commit the organized documentation structure"
