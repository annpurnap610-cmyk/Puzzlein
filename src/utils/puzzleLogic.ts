import { BestRecord, GridSize, MoveDirection, RecordsBySize } from '../types/puzzle';

const STORAGE_KEY = 'slidegrid_records_v1';

export function createSolvedBoard(size: GridSize): number[] {
  const total = size * size;
  const board: number[] = [];
  for (let i = 1; i < total; i++) {
    board.push(i);
  }
  board.push(0);
  return board;
}

export function isBoardSolved(board: number[]): boolean {
  for (let i = 0; i < board.length - 1; i++) {
    if (board[i] !== i + 1) {
      return false;
    }
  }
  return board[board.length - 1] === 0;
}

export function countPlacedTiles(board: number[]): number {
  let placed = 0;
  for (let i = 0; i < board.length - 1; i++) {
    if (board[i] === i + 1) {
      placed++;
    }
  }
  return placed;
}

export function getAdjacentIndices(index: number, size: GridSize): number[] {
  const row = Math.floor(index / size);
  const col = index % size;
  const neighbors: number[] = [];

  if (row > 0) neighbors.push((row - 1) * size + col);
  if (row < size - 1) neighbors.push((row + 1) * size + col);
  if (col > 0) neighbors.push(row * size + (col - 1));
  if (col < size - 1) neighbors.push(row * size + (col + 1));

  return neighbors;
}

export function createShuffledBoard(size: GridSize, scrambleMoves: number): number[] {
  const board = createSolvedBoard(size);
  let blankIndex = board.length - 1;
  let previousBlankIndex = -1;

  for (let step = 0; step < scrambleMoves; step++) {
    const neighbors = getAdjacentIndices(blankIndex, size).filter(
      (idx) => idx !== previousBlankIndex
    );
    const nextBlankIndex = neighbors[Math.floor(Math.random() * neighbors.length)];
    board[blankIndex] = board[nextBlankIndex];
    board[nextBlankIndex] = 0;
    previousBlankIndex = blankIndex;
    blankIndex = nextBlankIndex;
  }

  // Ensure the scrambled board does not accidentally land back on solved state
  if (isBoardSolved(board)) {
    const neighbors = getAdjacentIndices(blankIndex, size);
    const swapIdx = neighbors[0];
    board[blankIndex] = board[swapIdx];
    board[swapIdx] = 0;
  }

  return board;
}

export function canSlideTile(board: number[], tileIndex: number, size: GridSize): boolean {
  if (tileIndex < 0 || tileIndex >= board.length || board[tileIndex] === 0) {
    return false;
  }
  const blankIndex = board.indexOf(0);
  const tileRow = Math.floor(tileIndex / size);
  const tileCol = tileIndex % size;
  const blankRow = Math.floor(blankIndex / size);
  const blankCol = blankIndex % size;

  return tileRow === blankRow || tileCol === blankCol;
}

/**
 * Slides a clicked tile (and any intermediate tiles in the same row/column)
 * toward the empty space. Returns null if the tile cannot slide.
 */
export function slideTileAtIndex(
  board: number[],
  tileIndex: number,
  size: GridSize
): number[] | null {
  if (!canSlideTile(board, tileIndex, size)) {
    return null;
  }

  const blankIndex = board.indexOf(0);
  const tileRow = Math.floor(tileIndex / size);
  const tileCol = tileIndex % size;
  const blankRow = Math.floor(blankIndex / size);
  const blankCol = blankIndex % size;

  const next = [...board];

  if (tileRow === blankRow) {
    const step = tileCol < blankCol ? -1 : 1;
    for (let c = blankCol; c !== tileCol; c += step) {
      const target = blankRow * size + c;
      const source = blankRow * size + (c + step);
      next[target] = next[source];
    }
    next[tileIndex] = 0;
  } else {
    const step = tileRow < blankRow ? -1 : 1;
    for (let r = blankRow; r !== tileRow; r += step) {
      const target = r * size + blankCol;
      const source = (r + step) * size + blankCol;
      next[target] = next[source];
    }
    next[tileIndex] = 0;
  }

  return next;
}

/**
 * Moves a tile adjacent to the blank space in the given physical slide direction.
 * For example, 'UP' slides the tile below the blank upward into the blank space.
 */
export function slideByDirection(
  board: number[],
  size: GridSize,
  direction: MoveDirection
): number[] | null {
  const blankIndex = board.indexOf(0);
  const blankRow = Math.floor(blankIndex / size);
  const blankCol = blankIndex % size;

  let sourceRow = blankRow;
  let sourceCol = blankCol;

  if (direction === 'UP') sourceRow += 1;
  if (direction === 'DOWN') sourceRow -= 1;
  if (direction === 'LEFT') sourceCol += 1;
  if (direction === 'RIGHT') sourceCol -= 1;

  if (sourceRow < 0 || sourceRow >= size || sourceCol < 0 || sourceCol >= size) {
    return null;
  }

  return slideTileAtIndex(board, sourceRow * size + sourceCol, size);
}

export function formatTime(totalSeconds: number): string {
  const mins = Math.floor(totalSeconds / 60);
  const secs = totalSeconds % 60;
  return `${String(mins).padStart(2, '0')}:${String(secs).padStart(2, '0')}`;
}

export function loadBestRecords(): RecordsBySize {
  try {
    const raw = window.localStorage.getItem(STORAGE_KEY);
    if (!raw) return {};
    const parsed = JSON.parse(raw) as Record<string, unknown>;
    const clean: RecordsBySize = {};
    for (const size of [3, 4, 5] as GridSize[]) {
      const entry = parsed[String(size)] as Partial<BestRecord> | undefined;
      if (
        entry &&
        typeof entry.bestMoves === 'number' &&
        typeof entry.bestTimeSeconds === 'number' &&
        entry.bestMoves > 0
      ) {
        clean[size] = {
          bestMoves: entry.bestMoves,
          bestTimeSeconds: entry.bestTimeSeconds,
        };
      }
    }
    return clean;
  } catch {
    return {};
  }
}

export function saveBestRecord(
  size: GridSize,
  moves: number,
  timeSeconds: number
): { records: RecordsBySize; isNewBestMoves: boolean; isNewBestTime: boolean } {
  const records = loadBestRecords();
  const existing = records[size];

  const isNewBestMoves = !existing || moves < existing.bestMoves;
  const isNewBestTime = !existing || timeSeconds < existing.bestTimeSeconds;

  const updated: BestRecord = {
    bestMoves: existing ? Math.min(existing.bestMoves, moves) : moves,
    bestTimeSeconds: existing ? Math.min(existing.bestTimeSeconds, timeSeconds) : timeSeconds,
  };

  const nextRecords: RecordsBySize = {
    ...records,
    [size]: updated,
  };

  try {
    window.localStorage.setItem(STORAGE_KEY, JSON.stringify(nextRecords));
  } catch {
    // Ignore storage quota errors gracefully
  }

  return { records: nextRecords, isNewBestMoves, isNewBestTime };
}
